package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Auth.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        UsuarioAuth usuarioAuth = new UsuarioAuth();
        usuarioAuth.setCpf("12345678900");
        usuarioAuth.setSenha("senha-criptografada");
        usuarioAuth.setRole(Role.USUARIO);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuarioAuth);
        when(jwtService.generateToken(usuarioAuth)).thenReturn("token-gerado");

        String resultado = authService.login("12345678900", "senha-correta");

        assertEquals("token-gerado", resultado);
        verify(jwtService).generateToken(usuarioAuth);
    }

    @Test
    void deveRecusarCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class, () ->
                authService.login("12345678900", "senha-errada")
        );

        verifyNoInteractions(jwtService);
    }
}