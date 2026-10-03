package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Atendente.Pesistencia.AtendenteRepository;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAccountServiceTest {
    @Mock UsuarioAuthRepository authRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock MedicoRepository medicoRepository;
    @Mock AtendenteRepository atendenteRepository;
    @InjectMocks AuthAccountService service;

    @Test
    void normalizarRemoveEspacosEIgnoraMaiusculas() {
        assertEquals("ana@example.com", service.normalizar("  ANA@Example.COM  "));
        assertEquals("", service.normalizar(null));
    }

    @Test
    void encontrarPorEmailRetornaContaAutenticada() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(7L);
        auth.setEmail("ana@example.com");
        when(authRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(auth));

        assertSame(auth, service.encontrarPorEmail(" ANA@example.com ").orElseThrow());
        verify(usuarioRepository).findByEmailIgnoreCase("ana@example.com");
        verify(medicoRepository).findByEmailIgnoreCase("ana@example.com");
        verify(atendenteRepository).findByEmailIgnoreCase("ana@example.com");
    }

    @Test
    void garantirEmailDisponivelPermiteManterEmailDaMesmaConta() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(7L);
        when(authRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(auth));

        assertDoesNotThrow(() -> service.garantirEmailDisponivel(" ANA@example.com ", 7L));
    }

    @Test
    void garantirEmailDisponivelRejeitaEmailDeOutraConta() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(8L);
        when(authRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(auth));

        assertThrows(ConflitoException.class,
                () -> service.garantirEmailDisponivel("ana@example.com", 7L));
    }
}
