package com.example.appsaudebackend.auth.service;

import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.auth.service.AuthAccountService;
import com.example.appsaudebackend.modules.auth.service.AuthUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {
    @Mock AuthAccountService accountService;
    @Mock UsuarioAuthRepository authRepository;
    @InjectMocks AuthUserDetailsService service;

    @Test
    void carregaUsuarioPorEmail() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setEmail("ana@example.com");
        when(accountService.encontrarPorEmail("ana@example.com")).thenReturn(Optional.of(auth));

        assertSame(auth, service.loadUserByUsername("ana@example.com"));
    }

    @Test
    void emailDesconhecidoLancaExcecaoDeAutenticacao() {
        when(accountService.encontrarPorEmail("nao@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nao@example.com"));
    }

    @Test
    void carregaContaPorId() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(5L);
        when(authRepository.findById(5L)).thenReturn(Optional.of(auth));
        assertSame(auth, service.loadById(5L));
    }

    @Test
    void idDesconhecidoLancaExcecaoDeAutenticacao() {
        when(authRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadById(5L));
    }
}
