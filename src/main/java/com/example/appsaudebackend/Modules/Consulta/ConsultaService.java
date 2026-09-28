package com.example.appsaudebackend.Modules.Consulta;
import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.ConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.ConsultaResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.HistoricoPacienteResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.PacienteConsultaResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Model.Consulta;
import com.example.appsaudebackend.Modules.Consulta.Model.StatusConsulta;
import com.example.appsaudebackend.Modules.Consulta.Repository.ConsultaRepository;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;

    public List<ConsultaResponseDto> listarPorData(LocalDate data) {
        return consultaRepository
                .findByDataHoraBetweenOrderByDataHoraAsc(data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
                .map(c -> new ConsultaResponseDto(
                        c.getDataHora().format(HORA), c.getPaciente().getNome(),
                        c.getTipoConsulta() + " · " + c.getMedico().getNome(),
                        c.getStatus().name(), c.getDataHora()))
                .toList();
    }

    @Transactional
    public void criar(ConsultaRequestDto dto, UsuarioAuth solicitante) {
        UsuarioModel paciente = resolverPaciente(dto, solicitante);
        MedicoModel medico = medicoRepository.findById(dto.getMedicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));

        if (consultaRepository.existsByMedicoIdAndDataHora(dto.getMedicoId(), dto.getDataHora())) {
            throw new RegraNegocioException("O médico já possui uma consulta agendada nesse horário.");
        }

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setMedico(medico);
        consulta.setTipoConsulta(dto.getTipoConsulta());
        consulta.setDataHora(dto.getDataHora());
        consulta.setObservacao(dto.getObservacao());
        consulta.setStatus(StatusConsulta.AGENDADO);
        consultaRepository.save(consulta);
    }

    public List<PacienteConsultaResponseDto> listarDoPaciente(UsuarioAuth solicitante) {
        return consultaRepository.findByPacienteOrderByDataHoraAsc(pacienteLogado(solicitante))
                .stream()
                .map(c -> new PacienteConsultaResponseDto(
                        c.getId(), c.getDataHora(), c.getMedico().getNome(),
                        c.getMedico().getEspecialidade(), c.getStatus().name()))
                .toList();
    }

    public List<HistoricoPacienteResponseDto> listarHistorico(UsuarioAuth solicitante) {
        return consultaRepository
                .findByPacienteAndStatusOrderByDataHoraDesc(pacienteLogado(solicitante), StatusConsulta.CONCLUIDO)
                .stream()
                .map(c -> new HistoricoPacienteResponseDto(
                        c.getId(), c.getTipoConsulta(),
                        c.getMedico().getNome() + " · " + c.getMedico().getEspecialidade(),
                        c.getObservacao() == null ? "" : c.getObservacao(), c.getDataHora()))
                .toList();
    }

    private UsuarioModel resolverPaciente(ConsultaRequestDto dto, UsuarioAuth solicitante) {
        if (solicitante.getRole() == Role.USUARIO) {
            return pacienteLogado(solicitante);
        }
        if (solicitante.getRole() != Role.ATENDENTE) {
            throw new RegraNegocioException("Apenas paciente ou atendente pode criar agendamento.");
        }
        if (dto.getPacienteId() == null) {
            throw new RegraNegocioException("Paciente é obrigatório.");
        }
        return usuarioRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private UsuarioModel pacienteLogado(UsuarioAuth solicitante) {
        return usuarioRepository.findByUsuarioAuthCpf(solicitante.getCpf())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }
}
