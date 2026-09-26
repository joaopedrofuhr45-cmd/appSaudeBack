package com.example.appsaudebackend.Auth.Controller;

import com.example.appsaudebackend.Modules.Auth.Controller.AuthController;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void deveRealizarLogin() throws Exception {
        when(authService.login("12345678900", "123456")).thenReturn("token-fake");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "cpf": "12345678900",
                                    "senha": "123456"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                {
                                    "cpf": "",
                                    "senha": ""
                                }
                                """)
                )
                .andExpect(status().isBadRequest());
    }
}