package com.example.appsaudebackend.paciente.service;

import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.consulta.model.Consulta;
import com.example.appsaudebackend.modules.consulta.model.StatusConsulta;
import com.example.appsaudebackend.modules.consulta.service.ConsultaService;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.service.PacienteConsultaService;
import com.example.appsaudebackend.modules.paciente.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteConsultaServiceTest {
    @Mock PacienteService pacienteService;
    @Mock ConsultaService consultaService;
    @InjectMocks PacienteConsultaService service;

    private UsuarioAuth auth() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(1L);
        auth.setRole(Role.USUARIO);
        return auth;
    }

    private Consulta consulta(StatusConsulta status, String observacao) {
        MedicoModel medico = new MedicoModel();
        medico.setNome("Dr. João");
        medico.setEspecialidade("Cardiologia");
        Consulta consulta = new Consulta();
        consulta.setId(4L);
        consulta.setMedico(medico);
        consulta.setTipoConsulta("Retorno");
        consulta.setDataHora(LocalDateTime.of(2026, 11, 10, 10, 0));
        consulta.setStatus(status);
        consulta.setObservacao(observacao);
        return consulta;
    }

    @Test
    void listaConsultasDoPacienteLogado() {
        PacienteModel paciente = new PacienteModel();
        when(pacienteService.buscarPorUsuarioAuthId(1L)).thenReturn(paciente);
        when(consultaService.listarDoPaciente(paciente)).thenReturn(List.of(consulta(StatusConsulta.AGENDADO, null)));

        var resultado = service.listarConsultas(auth());

        assertEquals(1, resultado.size());
        assertEquals("Dr. João", resultado.get(0).medico());
        assertEquals("Cardiologia", resultado.get(0).especialidade());
        assertEquals("AGENDADO", resultado.get(0).status());
    }

    @Test
    void historicoUsaConsultasConcluidasEObservacaoVaziaQuandoNula() {
        PacienteModel paciente = new PacienteModel();
        when(pacienteService.buscarPorUsuarioAuthId(1L)).thenReturn(paciente);
        when(consultaService.listarConcluidasDoPaciente(paciente))
                .thenReturn(List.of(consulta(StatusConsulta.CONCLUIDO, null)));

        var resultado = service.listarHistorico(auth());

        assertEquals(1, resultado.size());
        assertEquals("Retorno", resultado.get(0).titulo());
        assertEquals("Dr. João · Cardiologia", resultado.get(0).detalhe());
        assertEquals("", resultado.get(0).descricao());
    }
}
