package com.example.appsaudebackend.consulta.service;

import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.consulta.service.ConsultaService;
import com.example.appsaudebackend.modules.consulta.dto.request.ConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.dto.request.FinalizarConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.model.Consulta;
import com.example.appsaudebackend.modules.consulta.model.StatusConsulta;
import com.example.appsaudebackend.modules.consulta.repository.ConsultaRepository;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import com.example.appsaudebackend.modules.medico.service.MedicoService;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.service.PacienteService;
import com.example.appsaudebackend.shared.exception.RegraNegocioException;
import com.example.appsaudebackend.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {
    @Mock ConsultaRepository consultaRepository;
    @Mock PacienteService pacienteService;
    @Mock MedicoService medicoService;
    @InjectMocks ConsultaService service;

    private ConsultaRequestDto pedido() {
        ConsultaRequestDto dto = new ConsultaRequestDto();
        dto.setMedicoId(7L);
        dto.setTipoConsulta("Clínica geral");
        dto.setDataHora(LocalDateTime.of(2026, 11, 10, 10, 0));
        return dto;
    }

    @Test
    void pacienteAgendaConsultaParaSiMesmo() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(1L);
        auth.setRole(Role.USUARIO);
        PacienteModel paciente = new PacienteModel();
        when(pacienteService.buscarPorUsuarioAuthId(1L)).thenReturn(paciente);
        when(medicoService.buscarPorIdParaAgendamento(7L)).thenReturn(new MedicoModel());
        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                7L, pedido().getDataHora(), StatusConsulta.CANCELADO)).thenReturn(false);

        service.criar(pedido(), auth);

        ArgumentCaptor<Consulta> saved = ArgumentCaptor.forClass(Consulta.class);
        verify(consultaRepository).save(saved.capture());
        assertSame(paciente, saved.getValue().getPaciente());
        assertEquals(StatusConsulta.AGENDADO, saved.getValue().getStatus());
        assertEquals("Clínica geral", saved.getValue().getTipoConsulta());
    }

    @Test
    void horarioOcupadoRejeitaAgendamento() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(1L);
        auth.setRole(Role.USUARIO);
        when(pacienteService.buscarPorUsuarioAuthId(1L)).thenReturn(new PacienteModel());
        when(medicoService.buscarPorIdParaAgendamento(7L)).thenReturn(new MedicoModel());
        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                7L, pedido().getDataHora(), StatusConsulta.CANCELADO)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.criar(pedido(), auth));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void pacienteNaoObtemConsultaDeOutroPaciente() {
        UsuarioAuth solicitante = new UsuarioAuth();
        solicitante.setId(2L);
        solicitante.setRole(Role.USUARIO);
        UsuarioAuth dono = new UsuarioAuth();
        dono.setId(1L);
        PacienteModel paciente = new PacienteModel();
        paciente.setUsuarioAuth(dono);
        Consulta consulta = consulta(paciente, medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter(4L, solicitante));
    }

    @Test
    void medicoFinalizaSuaConsultaERegistraObservacao() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(30L);
        auth.setRole(Role.MEDICO);
        UsuarioAuth medicoAuth = new UsuarioAuth();
        medicoAuth.setId(30L);
        MedicoModel medico = medico();
        medico.setUsuarioAuth(medicoAuth);
        PacienteModel paciente = new PacienteModel();
        paciente.setNome("Ana");
        Consulta consulta = consulta(paciente, medico, StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));
        when(medicoService.buscarPorUsuarioAuthId(30L)).thenReturn(medico);
        when(consultaRepository.save(consulta)).thenReturn(consulta);

        var result = service.finalizar(4L, new FinalizarConsultaRequestDto("  Retorno em 30 dias  "), auth);

        assertAll(
                () -> assertEquals("CONCLUIDO", result.status()),
                () -> assertEquals("Retorno em 30 dias", result.observacao()),
                () -> assertEquals(StatusConsulta.CONCLUIDO, consulta.getStatus())
        );
        verify(consultaRepository).save(consulta);
    }

    @Test
    void pacienteCancelaPropriaConsulta() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(1L);
        auth.setRole(Role.USUARIO);
        PacienteModel paciente = new PacienteModel();
        paciente.setUsuarioAuth(auth);
        Consulta consulta = consulta(paciente, medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(consulta)).thenReturn(consulta);

        var result = service.cancelar(4L, auth);

        assertEquals("CANCELADO", result.status());
        assertEquals(StatusConsulta.CANCELADO, consulta.getStatus());
    }

    @Test
    void pacienteNaoCancelaConsultaDeOutroPaciente() {
        UsuarioAuth solicitante = new UsuarioAuth();
        solicitante.setId(2L);
        solicitante.setRole(Role.USUARIO);
        UsuarioAuth dono = new UsuarioAuth();
        dono.setId(1L);
        PacienteModel paciente = new PacienteModel();
        paciente.setUsuarioAuth(dono);
        Consulta consulta = consulta(paciente, medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.cancelar(4L, solicitante));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void naoCancelaConsultaConcluidaOuJaCancelada() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(1L);
        auth.setRole(Role.ATENDENTE);
        for (StatusConsulta status : new StatusConsulta[]{StatusConsulta.CONCLUIDO, StatusConsulta.CANCELADO}) {
            Consulta consulta = consulta(new PacienteModel(), medico(), status);
            when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));
            assertThrows(RegraNegocioException.class, () -> service.cancelar(4L, auth));
        }
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void medicoNaoPodeCancelarConsulta() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(30L);
        auth.setRole(Role.MEDICO);
        Consulta consulta = consulta(new PacienteModel(), medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.cancelar(4L, auth));
    }

    @Test
    void consultaInexistenteRetornaErroDeRecursoNaoEncontrado() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter(99L, new UsuarioAuth()));
    }

    @Test
    void agendaGeralSoParaAtendente() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(30L);
        auth.setRole(Role.MEDICO);

        assertThrows(RegraNegocioException.class, () -> service.listarPorData(LocalDate.of(2026, 11, 10), auth));
        verifyNoInteractions(consultaRepository);
    }

    @Test
    void agendaDoMedicoBuscaPeloIdDoMedico() {
        LocalDate dia = LocalDate.of(2026, 11, 10);
        PacienteModel paciente = new PacienteModel();
        paciente.setNome("Ana");
        Consulta consulta = consulta(paciente, medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findByMedicoIdAndDataHoraBetweenOrderByDataHoraAsc(
                7L, dia.atStartOfDay(), dia.atTime(LocalTime.MAX))).thenReturn(List.of(consulta));

        var agenda = service.listarAgendaDoMedico(7L, dia);

        assertEquals(1, agenda.size());
        assertEquals("10:00", agenda.get(0).horario());
        assertEquals("Ana", agenda.get(0).nomePaciente());
    }

    private MedicoModel medico() {
        MedicoModel medico = new MedicoModel();
        medico.setId(7L);
        medico.setNome("Dr. João");
        medico.setEspecialidade("Cardiologia");
        return medico;
    }

    private Consulta consulta(PacienteModel paciente, MedicoModel medico, StatusConsulta status) {
        Consulta consulta = new Consulta();
        consulta.setId(4L);
        consulta.setPaciente(paciente);
        consulta.setMedico(medico);
        consulta.setTipoConsulta("Consulta");
        consulta.setDataHora(LocalDateTime.of(2026, 11, 10, 10, 0));
        consulta.setStatus(status);
        return consulta;
    }
}
