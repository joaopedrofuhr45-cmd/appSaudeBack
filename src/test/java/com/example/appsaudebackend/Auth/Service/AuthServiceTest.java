package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Auth.Service.GoogleIdentityService;
import com.example.appsaudebackend.Modules.Auth.Service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository authRepository;
    @Mock com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthAccountService accountService;
    @Mock GoogleIdentityService googleIdentityService;
    @Mock Authentication authentication;
    @InjectMocks AuthService authService;

    @Test
    void loginNormalizaEmailAutenticaEEmiteToken() {
        UsuarioAuth usuario = new UsuarioAuth();
        usuario.setEmail("ana@example.com");
        usuario.setRole(Role.USUARIO);
        when(accountService.normalizar(" Ana@Example.com ")).thenReturn("ana@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("token-gerado");

        String token = authService.login(" Ana@Example.com ", "senha-correta");

        assertEquals("token-gerado", token);
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("ana@example.com", captor.getValue().getPrincipal());
        assertEquals("senha-correta", captor.getValue().getCredentials());
        verify(jwtService).generateToken(usuario);
    }

    @Test
    void credenciaisInvalidasNaoGeramToken() {
        when(accountService.normalizar("ana@example.com")).thenReturn("ana@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login("ana@example.com", "senha-errada"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginGoogleGeraTokenParaContaAutenticada() {
        UsuarioAuth usuario = new UsuarioAuth();
        when(googleIdentityService.autenticar("credential")).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("google-token");

        assertEquals("google-token", authService.loginComGoogle("credential"));
        verify(jwtService).generateToken(usuario);
    }
}
