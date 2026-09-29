package com.example.appsaudebackend.Modules.Consulta;

import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.ConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.FinalizarConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.*;
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

    public List<ConsultaResponseDto> listarPorData(LocalDate data, UsuarioAuth solicitante) {
        List<Consulta> consultas = consultaRepository.findByDataHoraBetweenOrderByDataHoraAsc(
                data.atStartOfDay(), data.atTime(LocalTime.MAX));

        if (solicitante.getRole() == Role.MEDICO) {
            MedicoModel medico = medicoLogado(solicitante);
            consultas = consultas.stream().filter(c -> c.getMedico().getId().equals(medico.getId())).toList();
        } else if (solicitante.getRole() != Role.ATENDENTE) {
            throw new RegraNegocioException("Apenas atendente ou médico pode consultar a agenda.");
        }

        return consultas.stream().map(c -> new ConsultaResponseDto(
                c.getId(), c.getDataHora().format(HORA), c.getPaciente().getNome(),
                c.getTipoConsulta() + " · " + c.getMedico().getNome(),
                c.getStatus().name(), c.getDataHora())).toList();
    }

    @Transactional(readOnly = true)
    public ConsultaDetalheResponseDto obter(Long id, UsuarioAuth solicitante) {
        Consulta consulta = buscar(id);
        validarAcesso(consulta, solicitante);
        return toDetalhe(consulta);
    }

    @Transactional
    public ConsultaDetalheResponseDto finalizar(Long id, FinalizarConsultaRequestDto dto, UsuarioAuth solicitante) {
        Consulta consulta = buscar(id);
        if (solicitante.getRole() != Role.MEDICO) {
            throw new RegraNegocioException("Apenas o médico pode finalizar a consulta.");
        }
        MedicoModel medico = medicoLogado(solicitante);
        if (!consulta.getMedico().getId().equals(medico.getId())) {
            throw new RegraNegocioException("A consulta não pertence ao médico autenticado.");
        }
        if (consulta.getStatus() == StatusConsulta.CANCELADO) {
            throw new RegraNegocioException("Não é possível finalizar uma consulta cancelada.");
        }
        if (consulta.getStatus() == StatusConsulta.CONCLUIDO) {
            throw new RegraNegocioException("A consulta já foi finalizada.");
        }
        consulta.setStatus(StatusConsulta.CONCLUIDO);
        if (dto.observacao() != null && !dto.observacao().isBlank()) {
            consulta.setObservacao(dto.observacao().trim());
        }
        return toDetalhe(consultaRepository.save(consulta));
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
        return consultaRepository.findByPacienteOrderByDataHoraAsc(pacienteLogado(solicitante)).stream()
                .map(c -> new PacienteConsultaResponseDto(c.getId(), c.getDataHora(), c.getMedico().getNome(),
                        c.getMedico().getEspecialidade(), c.getStatus().name())).toList();
    }

    public List<HistoricoPacienteResponseDto> listarHistorico(UsuarioAuth solicitante) {
        return consultaRepository.findByPacienteAndStatusOrderByDataHoraDesc(
                pacienteLogado(solicitante), StatusConsulta.CONCLUIDO).stream()
                .map(c -> new HistoricoPacienteResponseDto(c.getId(), c.getTipoConsulta(),
                        c.getMedico().getNome() + " · " + c.getMedico().getEspecialidade(),
                        c.getObservacao() == null ? "" : c.getObservacao(), c.getDataHora())).toList();
    }

    private Consulta buscar(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }

    private void validarAcesso(Consulta consulta, UsuarioAuth solicitante) {
        if (solicitante.getRole() == Role.MEDICO) {
            if (!consulta.getMedico().getUsuarioAuth().getCpf().equals(solicitante.getCpf()))
                throw new RegraNegocioException("A consulta não pertence ao médico autenticado.");
            return;
        }
        if (solicitante.getRole() == Role.ATENDENTE) return;
        if (solicitante.getRole() == Role.USUARIO &&
                !consulta.getPaciente().getUsuarioAuth().getCpf().equals(solicitante.getCpf()))
            throw new RegraNegocioException("A consulta não pertence ao paciente autenticado.");
        if (solicitante.getRole() != Role.USUARIO)
            throw new RegraNegocioException("Perfil sem acesso à consulta.");
    }

    private ConsultaDetalheResponseDto toDetalhe(Consulta c) {
        return new ConsultaDetalheResponseDto(c.getId(), c.getPaciente().getNome(), c.getMedico().getNome(),
                c.getMedico().getEspecialidade(), c.getTipoConsulta(), c.getDataHora(), c.getStatus().name(),
                c.getObservacao() == null ? "" : c.getObservacao());
    }

    private UsuarioModel resolverPaciente(ConsultaRequestDto dto, UsuarioAuth solicitante) {
        if (solicitante.getRole() == Role.USUARIO) return pacienteLogado(solicitante);
        if (solicitante.getRole() != Role.ATENDENTE)
            throw new RegraNegocioException("Apenas paciente ou atendente pode criar agendamento.");
        if (dto.getPacienteId() == null) throw new RegraNegocioException("Paciente é obrigatório.");
        return usuarioRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private UsuarioModel pacienteLogado(UsuarioAuth solicitante) {
        return usuarioRepository.findByUsuarioAuthCpf(solicitante.getCpf())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private MedicoModel medicoLogado(UsuarioAuth solicitante) {
        return medicoRepository.findByUsuarioAuthCpf(solicitante.getCpf())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }
}