package com.example.appsaudebackend.integracao;

import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo completo de consulta: paciente agenda, atendente vê a agenda, médico finaliza, paciente cancela. */
class ConsultaIntegracaoTest extends IntegracaoBase {

    // cada teste usa um dia diferente no futuro, então a agenda geral de um teste nunca mistura com a de outro
    private static final AtomicInteger PROXIMO_DIA = new AtomicInteger(400);

    private static LocalDate diaUnico() {
        return LocalDate.now().plusDays(PROXIMO_DIA.incrementAndGet());
    }

    private static String corpoConsulta(long medicoId, LocalDate dia, String hora) {
        return "{\"medicoId\":" + medicoId + ",\"tipoConsulta\":\"Rotina\",\"dataHora\":\"" + dia + "T" + hora + ":00\"}";
    }

    private void agendar(Sessao paciente, MedicoModel medico, LocalDate dia, String hora) throws Exception {
        mockMvc.perform(comCsrf(post("/consultas"), paciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(medico.getId(), dia, hora)))
                .andExpect(status().isCreated());
    }

    private long idDaPrimeiraConsultaDoDia(Sessao atendente, LocalDate dia) throws Exception {
        MvcResult result = mockMvc.perform(get("/consultas").param("data", dia.toString()).cookie(atendente.auth()))
                .andExpect(status().isOk())
                .andReturn();
        return idDe(result, "$[0].id");
    }

    @Test
    void pacienteAgendaEAtendenteVeNaAgendaDoDia() throws Exception {
        MedicoModel medico = criarMedico("Dr. Rui", "Clínico geral");
        Sessao paciente = entrar(cadastrarPaciente("Ana"));
        Sessao atendente = entrar(criarAtendente("Bia").getEmail());
        LocalDate dia = diaUnico();

        agendar(paciente, medico, dia, "09:00");

        mockMvc.perform(get("/consultas").param("data", dia.toString()).cookie(atendente.auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomePaciente").value("Ana"))
                .andExpect(jsonPath("$[0].horario").value("09:00"))
                .andExpect(jsonPath("$[0].status").value("AGENDADO"));
    }

    @Test
    void atendenteAgendaParaUmPaciente() throws Exception {
        MedicoModel medico = criarMedico("Dra. Sol", "Pediatria");
        String emailPaciente = cadastrarPaciente("Caio");
        long pacienteId = buscarPaciente(emailPaciente).getId();
        Sessao atendente = entrar(criarAtendente("Dani").getEmail());
        LocalDate dia = diaUnico();

        mockMvc.perform(comCsrf(post("/consultas"), atendente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":" + pacienteId + ",\"medicoId\":" + medico.getId()
                                + ",\"tipoConsulta\":\"Retorno\",\"dataHora\":\"" + dia + "T14:30:00\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/consultas").param("data", dia.toString()).cookie(atendente.auth()))
                .andExpect(jsonPath("$[0].nomePaciente").value("Caio"));
    }

    @Test
    void atendenteSemInformarPacienteRecebeBadRequest() throws Exception {
        MedicoModel medico = criarMedico("Dr. Leo", "Ortopedia");
        Sessao atendente = entrar(criarAtendente("Eli").getEmail());

        mockMvc.perform(comCsrf(post("/consultas"), atendente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(medico.getId(), diaUnico(), "10:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void horarioDoMedicoJaOcupadoRetornaBadRequest() throws Exception {
        MedicoModel medico = criarMedico("Dra. Vera", "Dermatologia");
        Sessao primeiro = entrar(cadastrarPaciente("Fabi"));
        Sessao segundo = entrar(cadastrarPaciente("Gui"));
        LocalDate dia = diaUnico();

        agendar(primeiro, medico, dia, "11:00");

        mockMvc.perform(comCsrf(post("/consultas"), segundo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(medico.getId(), dia, "11:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agendamentoNoPassadoRetornaBadRequest() throws Exception {
        MedicoModel medico = criarMedico("Dr. Nuno", "Cardiologia");
        Sessao paciente = entrar(cadastrarPaciente("Hugo"));

        mockMvc.perform(comCsrf(post("/consultas"), paciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(medico.getId(), LocalDate.now().minusDays(3), "10:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agendamentoComMedicoInexistenteRetornaNaoEncontrado() throws Exception {
        Sessao paciente = entrar(cadastrarPaciente("Iris"));

        mockMvc.perform(comCsrf(post("/consultas"), paciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(999_999L, diaUnico(), "10:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void medicoNaoPodeAgendarConsulta() throws Exception {
        MedicoModel medico = criarMedico("Dra. Lu", "Neurologia");
        Sessao sessaoMedico = entrar(medico.getEmail());

        mockMvc.perform(comCsrf(post("/consultas"), sessaoMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoConsulta(medico.getId(), diaUnico(), "08:00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteNaoVeAAgendaGeralDoAtendente() throws Exception {
        Sessao paciente = entrar(cadastrarPaciente("Jo"));

        mockMvc.perform(get("/consultas").param("data", diaUnico().toString()).cookie(paciente.auth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void agendaSemLoginEBloqueada() throws Exception {
        mockMvc.perform(get("/consultas").param("data", diaUnico().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void medicoFinalizaAConsultaEFicaConcluida() throws Exception {
        MedicoModel medico = criarMedico("Dr. Mario", "Clínico geral");
        Sessao paciente = entrar(cadastrarPaciente("Kia"));
        Sessao atendente = entrar(criarAtendente("Lara").getEmail());
        Sessao sessaoMedico = entrar(medico.getEmail());
        LocalDate dia = diaUnico();
        agendar(paciente, medico, dia, "15:00");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/finalizar"), sessaoMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacao\":\"Paciente estavel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.observacao").value("Paciente estavel"));

        // finalizar de novo não pode
        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/finalizar"), sessaoMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pacienteNaoPodeFinalizarConsulta() throws Exception {
        MedicoModel medico = criarMedico("Dra. Ney", "Oftalmologia");
        Sessao paciente = entrar(cadastrarPaciente("Leo"));
        Sessao atendente = entrar(criarAtendente("Mel").getEmail());
        LocalDate dia = diaUnico();
        agendar(paciente, medico, dia, "16:00");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/finalizar"), paciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void outroMedicoNaoPodeFinalizarConsultaDeColega() throws Exception {
        MedicoModel dono = criarMedico("Dr. Otto", "Urologia");
        MedicoModel intruso = criarMedico("Dr. Paulo", "Urologia");
        Sessao paciente = entrar(cadastrarPaciente("Noa"));
        Sessao atendente = entrar(criarAtendente("Ola").getEmail());
        Sessao sessaoIntruso = entrar(intruso.getEmail());
        LocalDate dia = diaUnico();
        agendar(paciente, dono, dia, "08:30");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/finalizar"), sessaoIntruso)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void pacienteCancelaASuaConsultaEUmaSegundaVezDaErro() throws Exception {
        MedicoModel medico = criarMedico("Dra. Rita", "Pediatria");
        Sessao paciente = entrar(cadastrarPaciente("Pri"));
        Sessao atendente = entrar(criarAtendente("Quel").getEmail());
        LocalDate dia = diaUnico();
        agendar(paciente, medico, dia, "13:00");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/cancelar"), paciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/cancelar"), paciente))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pacienteNaoCancelaConsultaDeOutroPaciente() throws Exception {
        MedicoModel medico = criarMedico("Dr. Sam", "Clínico geral");
        Sessao dono = entrar(cadastrarPaciente("Rui"));
        Sessao outro = entrar(cadastrarPaciente("Sara"));
        Sessao atendente = entrar(criarAtendente("Tia").getEmail());
        LocalDate dia = diaUnico();
        agendar(dono, medico, dia, "10:30");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(comCsrf(patch("/consultas/" + id + "/cancelar"), outro))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarSemTokenCsrfEBloqueado() throws Exception {
        MedicoModel medico = criarMedico("Dra. Uma", "Cardiologia");
        Sessao paciente = entrar(cadastrarPaciente("Val"));
        Sessao atendente = entrar(criarAtendente("Wal").getEmail());
        LocalDate dia = diaUnico();
        agendar(paciente, medico, dia, "17:00");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(patch("/consultas/" + id + "/cancelar").cookie(paciente.auth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void detalheDaConsultaRespeitaQuemPodeVer() throws Exception {
        MedicoModel medico = criarMedico("Dr. Xavi", "Ortopedia");
        Sessao dono = entrar(cadastrarPaciente("Yan"));
        Sessao outro = entrar(cadastrarPaciente("Zeca"));
        Sessao atendente = entrar(criarAtendente("Ada").getEmail());
        LocalDate dia = diaUnico();
        agendar(dono, medico, dia, "12:00");
        long id = idDaPrimeiraConsultaDoDia(atendente, dia);

        mockMvc.perform(get("/consultas/" + id).cookie(dono.auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente").value("Yan"))
                .andExpect(jsonPath("$.medico").value("Dr. Xavi"));

        mockMvc.perform(get("/consultas/" + id).cookie(outro.auth()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/consultas/" + id).cookie(atendente.auth()))
                .andExpect(status().isOk());
    }
}
