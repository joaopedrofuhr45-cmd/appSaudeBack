package com.example.appsaudebackend.Auth.Controller;

import com.example.appsaudebackend.Modules.Auth.Controller.AuthController;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Auth.Service.JwtService;
import com.example.appsaudebackend.Modules.Auth.Service.AuthUserDetailsService;

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
}
