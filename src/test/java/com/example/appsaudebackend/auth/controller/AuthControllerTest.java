package com.example.appsaudebackend.auth.controller;

import com.example.appsaudebackend.modules.auth.controller.AuthController;
import com.example.appsaudebackend.modules.auth.service.AuthService;
import com.example.appsaudebackend.modules.auth.service.LoginAttemptService;
import com.example.appsaudebackend.shared.exception.MuitasTentativasException;
import org.springframework.security.authentication.BadCredentialsException;
import com.example.appsaudebackend.modules.auth.service.JwtService;
import com.example.appsaudebackend.modules.auth.service.AuthUserDetailsService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"jwt.expiration=3600000", "app.cookie.secure=false", "app.cors.allowed-origins=http://localhost:4200"}, controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @Test
    void deveRealizarLogin() throws Exception {
        when(authService.login(
                "ana@example.com",
                "123456"
        )).thenReturn("token-fake");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "ana@example.com",
                                            "senha": "123456"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).login(
                "ana@example.com",
                "123456"
        );
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "",
                                            "senha": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    private static final String LOGIN_JSON = """
            { "email": "ana@example.com", "senha": "errada123" }
            """;

    @Test
    void deveResponder429ComRetryAfterQuandoBloqueado() throws Exception {
        org.mockito.Mockito.doThrow(new MuitasTentativasException(120))
                .when(loginAttemptService).verificarBloqueio(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("ana@example.com"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "120"));

        org.mockito.Mockito.verifyNoInteractions(authService);
    }

    @Test
    void deveRegistrarFalhaQuandoCredenciaisInvalidas() throws Exception {
        when(authService.login("ana@example.com", "errada123")).thenThrow(new BadCredentialsException("x"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).registrarFalha(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("ana@example.com"));
    }

    @Test
    void deveZerarContadorQuandoLoginCorreto() throws Exception {
        when(authService.login("ana@example.com", "errada123")).thenReturn("token");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isOk());

        verify(loginAttemptService).registrarSucesso(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("ana@example.com"));
    }
}
