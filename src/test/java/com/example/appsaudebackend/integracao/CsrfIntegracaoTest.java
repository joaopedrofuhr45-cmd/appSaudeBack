package com.example.appsaudebackend.integracao;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Garante que o CSRF está ligado e que o fluxo cookie + header funciona. */
class CsrfIntegracaoTest extends IntegracaoBase {

    @Test
    void endpointDeCsrfEmiteTokenECookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie, "o cookie XSRF-TOKEN precisa ser emitido");
        assertFalse(cookie.isHttpOnly(), "o cookie XSRF-TOKEN não pode ser HttpOnly (o front precisa ler)");
    }

    @Test
    void postAutenticadoSemTokenCsrfEBloqueado() throws Exception {
        Sessao sessao = entrar(cadastrarPaciente("Julia"));

        mockMvc.perform(post("/auth/logout").cookie(sessao.auth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAutenticadoComTokenCsrfErradoEBloqueado() throws Exception {
        Sessao sessao = entrar(cadastrarPaciente("Kleber"));

        mockMvc.perform(post("/auth/logout")
                        .cookie(sessao.auth(), sessao.xsrf())
                        .header("X-XSRF-TOKEN", "token-forjado"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postComHeaderMasSemOCookieCsrfEBloqueado() throws Exception {
        Sessao sessao = entrar(cadastrarPaciente("Lia"));

        mockMvc.perform(post("/auth/logout")
                        .cookie(sessao.auth())
                        .header("X-XSRF-TOKEN", sessao.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAutenticadoComCookieEHeaderCsrfFunciona() throws Exception {
        Sessao sessao = entrar(cadastrarPaciente("Marcos"));

        MvcResult result = mockMvc.perform(comCsrf(post("/auth/logout"), sessao))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie limpo = result.getResponse().getCookie("auth_token");
        assertNotNull(limpo);
        assertEquals(0, limpo.getMaxAge(), "o logout precisa expirar o cookie de login");
    }

    @Test
    void getNaoExigeTokenCsrf() throws Exception {
        Cookie auth = login(cadastrarPaciente("Nina"), SENHA);

        mockMvc.perform(get("/auth/me").cookie(auth)).andExpect(status().isOk());
    }

    @Test
    void loginECadastroNaoExigemTokenCsrf() throws Exception {
        // cadastrarPaciente e login não mandam token nenhum e precisam continuar funcionando
        String email = cadastrarPaciente("Otavio");
        login(email, SENHA);
    }

    @Test
    void metodosQueAlteramDadosEmConsultasExigemCsrf() throws Exception {
        Sessao sessao = entrar(cadastrarPaciente("Paula"));

        mockMvc.perform(post("/consultas").cookie(sessao.auth())
                        .contentType("application/json")
                        .content("{\"medicoId\":1,\"tipoConsulta\":\"Rotina\",\"dataHora\":\"2099-01-01T10:00:00\"}"))
                .andExpect(status().isForbidden());
    }
}
