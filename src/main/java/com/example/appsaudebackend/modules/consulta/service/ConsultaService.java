package com.example.appsaudebackend.modules.consulta.service;

import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.consulta.dto.request.ConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.dto.request.FinalizarConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.dto.response.ConsultaDetalheResponseDto;
import com.example.appsaudebackend.modules.consulta.dto.response.ConsultaResponseDto;
import com.example.appsaudebackend.modules.consulta.model.Consulta;
import com.example.appsaudebackend.modules.consulta.model.StatusConsulta;
import com.example.appsaudebackend.modules.consulta.repository.ConsultaRepository;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import com.example.appsaudebackend.modules.medico.service.MedicoService;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.service.PacienteService;
import com.example.appsaudebackend.shared.exception.RegraNegocioException;
import com.example.appsaudebackend.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Regras da consulta em si. Paciente e médico são buscados pelos Services
 * dos respectivos módulos (nunca pelos Repositories deles).
 * As listagens "do paciente" e "do médico" ficam nos módulos Paciente e Medico,
 * que chamam os métodos públicos de consulta daqui.
 */
@Service
@RequiredArgsConstructor
public class ConsultaService {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final ConsultaRepository consultaRepository;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    /** Agenda geral do dia (atendente). A agenda do médico fica em /medicos/me/consultas. */
    public List<ConsultaResponseDto> listarPorData(LocalDate data, UsuarioAuth solicitante) {
        if (solicitante.getRole() != Role.ATENDENTE) {
            throw new RegraNegocioException("Apenas atendente pode consultar a agenda geral.");
        }
        return toAgenda(consultaRepository.findByDataHoraBetweenOrderByDataHoraAsc(
                data.atStartOfDay(), data.atTime(LocalTime.MAX)));
    }

    /** Agenda do dia de um médico (usada pelo módulo Medico). */
    public List<ConsultaResponseDto> listarAgendaDoMedico(Long medicoId, LocalDate data) {
        return toAgenda(consultaRepository.findByMedicoIdAndDataHoraBetweenOrderByDataHoraAsc(
                medicoId, data.atStartOfDay(), data.atTime(LocalTime.MAX)));
    }

    /** Todas as consultas de um paciente, da mais antiga para a mais nova (usada pelo módulo Paciente). */
    public List<Consulta> listarDoPaciente(PacienteModel paciente) {
        return consultaRepository.findByPacienteOrderByDataHoraAsc(paciente);
    }

    /** Consultas concluídas de um paciente, da mais nova para a mais antiga (usada pelo módulo Paciente). */
    public List<Consulta> listarConcluidasDoPaciente(PacienteModel paciente) {
        return consultaRepository.findByPacienteAndStatusOrderByDataHoraDesc(paciente, StatusConsulta.CONCLUIDO);
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
        MedicoModel medico = medicoService.buscarPorUsuarioAuthId(solicitante.getId());
        if (!consulta.getMedico().getId().equals(medico.getId())) {
            // 404 genérico: não revela que o ID existe para quem não é o dono
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
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
    public ConsultaDetalheResponseDto cancelar(Long id, UsuarioAuth solicitante) {
        Consulta consulta = buscar(id);
        if (solicitante.getRole() != Role.USUARIO && solicitante.getRole() != Role.ATENDENTE) {
            throw new RegraNegocioException("Apenas paciente ou atendente pode cancelar a consulta.");
        }
        validarAcesso(consulta, solicitante);
        if (consulta.getStatus() == StatusConsulta.CANCELADO) {
            throw new RegraNegocioException("A consulta já foi cancelada.");
        }
        if (consulta.getStatus() == StatusConsulta.CONCLUIDO) {
            throw new RegraNegocioException("Não é possível cancelar uma consulta já concluída.");
        }
        consulta.setStatus(StatusConsulta.CANCELADO);
        return toDetalhe(consultaRepository.save(consulta));
    }

    @Transactional
    public void criar(ConsultaRequestDto dto, UsuarioAuth solicitante) {
        PacienteModel paciente = resolverPaciente(dto, solicitante);
        // Trava a linha do médico até o fim da transação: dois agendamentos simultâneos
        // no mesmo horário ficam em fila e o segundo enxerga o primeiro na checagem abaixo.
        MedicoModel medico = medicoService.buscarPorIdParaAgendamento(dto.getMedicoId());
        // consulta cancelada libera o horário
        if (consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                dto.getMedicoId(), dto.getDataHora(), StatusConsulta.CANCELADO)) {
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

    private Consulta buscar(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }

    private void validarAcesso(Consulta consulta, UsuarioAuth solicitante) {
        if (solicitante.getRole() == Role.MEDICO) {
            if (!consulta.getMedico().getUsuarioAuth().getId().equals(solicitante.getId()))
                throw new RecursoNaoEncontradoException("Consulta não encontrada.");
            return;
        }
        if (solicitante.getRole() == Role.ATENDENTE) return;
        if (solicitante.getRole() == Role.USUARIO &&
                !consulta.getPaciente().getUsuarioAuth().getId().equals(solicitante.getId()))
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        if (solicitante.getRole() != Role.USUARIO)
            throw new RegraNegocioException("Perfil sem acesso à consulta.");
    }

    private List<ConsultaResponseDto> toAgenda(List<Consulta> consultas) {
        return consultas.stream().map(c -> new ConsultaResponseDto(
                c.getId(), c.getDataHora().format(HORA), c.getPaciente().getNome(),
                c.getTipoConsulta() + " · " + c.getMedico().getNome(),
                c.getStatus().name(), c.getDataHora())).toList();
    }

    private ConsultaDetalheResponseDto toDetalhe(Consulta c) {
        return new ConsultaDetalheResponseDto(c.getId(), c.getPaciente().getNome(), c.getMedico().getNome(),
                c.getMedico().getEspecialidade(), c.getTipoConsulta(), c.getDataHora(), c.getStatus().name(),
                c.getObservacao() == null ? "" : c.getObservacao());
    }

    private PacienteModel resolverPaciente(ConsultaRequestDto dto, UsuarioAuth solicitante) {
        if (solicitante.getRole() == Role.USUARIO) {
            return pacienteService.buscarPorUsuarioAuthId(solicitante.getId());
        }
        if (solicitante.getRole() != Role.ATENDENTE)
            throw new RegraNegocioException("Apenas paciente ou atendente pode criar agendamento.");
        if (dto.getPacienteId() == null) throw new RegraNegocioException("Paciente é obrigatório.");
        return pacienteService.buscarPorId(dto.getPacienteId());
    }
}
