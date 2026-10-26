package com.example.appsaudebackend.integracao;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegracaoTest extends IntegracaoBase {

    @Test
    void cadastroLoginEMeFuncionamDePontaAPonta() throws Exception {
        String email = cadastrarPaciente("Ana Souza");

        Cookie auth = login(email, SENHA);
        assertTrue(auth.isHttpOnly(), "o JWT precisa ir em cookie HttpOnly");

        mockMvc.perform(get("/auth/me").cookie(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USUARIO"));
    }

    @Test
    void cadastroCriaTambemOPerfilDoPaciente() throws Exception {
        String email = cadastrarPaciente("Bruno Lima");

        assertTrue(pacienteRepository.findByEmailIgnoreCase(email).isPresent());
    }

    @Test
    void cadastroNormalizaOEmailParaMinusculas() throws Exception {
        String email = emailUnico("MAIUSCULO");
        mockMvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("nome", "Carla", "email", email.toUpperCase(), "senha", SENHA)))
                .andExpect(status().isCreated());

        login(email.toLowerCase(), SENHA);
    }

    @Test
    void cadastroComEmailRepetidoRetornaConflito() throws Exception {
        String email = cadastrarPaciente("Diego");

        mockMvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("nome", "Outro Diego", "email", email, "senha", SENHA)))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroComSenhaCurtaRetornaBadRequest() throws Exception {
        mockMvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("nome", "Eva", "email", emailUnico("eva"), "senha", "123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastroSemNomeRetornaBadRequest() throws Exception {
        mockMvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", emailUnico("sem.nome"), "senha", SENHA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComSenhaErradaRetornaNaoAutorizado() throws Exception {
        String email = cadastrarPaciente("Fabio");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", email, "senha", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComEmailInexistenteRetornaNaoAutorizado() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("email", emailUnico("fantasma"), "senha", SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meSemLoginEBloqueado() throws Exception {
        mockMvc.perform(get("/auth/me")).andExpect(status().is4xxClientError());
    }

    @Test
    void meComCookieInvalidoEBloqueado() throws Exception {
        mockMvc.perform(get("/auth/me").cookie(new Cookie("auth_token", "token.invalido.aqui")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void medicoEAtendenteLogamComContasPreCadastradas() throws Exception {
        String emailMedico = criarMedico("Dra. Helena", "Cardiologia").getEmail();
        String emailAtendente = criarAtendente("Igor").getEmail();

        mockMvc.perform(get("/auth/me").cookie(login(emailMedico, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("MEDICO"));
        mockMvc.perform(get("/auth/me").cookie(login(emailAtendente, SENHA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ATENDENTE"));
    }
}
