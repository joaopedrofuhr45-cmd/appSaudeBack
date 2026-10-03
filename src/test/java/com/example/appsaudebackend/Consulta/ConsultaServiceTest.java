package com.example.appsaudebackend.Consulta;

import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.ConsultaService;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.ConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.FinalizarConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Model.Consulta;
import com.example.appsaudebackend.Modules.Consulta.Model.StatusConsulta;
import com.example.appsaudebackend.Modules.Consulta.Repository.ConsultaRepository;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {
    @Mock ConsultaRepository consultaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock MedicoRepository medicoRepository;
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
        UsuarioModel paciente = new UsuarioModel();
        when(usuarioRepository.findByUsuarioAuthId(1L)).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(7L)).thenReturn(Optional.of(new MedicoModel()));
        when(consultaRepository.existsByMedicoIdAndDataHora(7L, pedido().getDataHora())).thenReturn(false);

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
        when(usuarioRepository.findByUsuarioAuthId(1L)).thenReturn(Optional.of(new UsuarioModel()));
        when(medicoRepository.findById(7L)).thenReturn(Optional.of(new MedicoModel()));
        when(consultaRepository.existsByMedicoIdAndDataHora(7L, pedido().getDataHora())).thenReturn(true);

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
        UsuarioModel paciente = new UsuarioModel();
        paciente.setUsuarioAuth(dono);
        Consulta consulta = consulta(paciente, medico(), StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> service.obter(4L, solicitante));
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
        UsuarioModel paciente = new UsuarioModel();
        paciente.setNome("Ana");
        Consulta consulta = consulta(paciente, medico, StatusConsulta.AGENDADO);
        when(consultaRepository.findById(4L)).thenReturn(Optional.of(consulta));
        when(medicoRepository.findByUsuarioAuthId(30L)).thenReturn(Optional.of(medico));
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
    void consultaInexistenteRetornaErroDeRecursoNaoEncontrado() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter(99L, new UsuarioAuth()));
    }

    private MedicoModel medico() {
        MedicoModel medico = new MedicoModel();
        medico.setId(7L);
        medico.setNome("Dr. João");
        medico.setEspecialidade("Cardiologia");
        return medico;
    }

    private Consulta consulta(UsuarioModel paciente, MedicoModel medico, StatusConsulta status) {
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
