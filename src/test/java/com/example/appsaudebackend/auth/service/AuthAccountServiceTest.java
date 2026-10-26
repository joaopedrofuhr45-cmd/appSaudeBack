package com.example.appsaudebackend.auth.service;

import com.example.appsaudebackend.modules.atendente.persistencia.AtendenteRepository;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.auth.service.AuthAccountService;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoRepository;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteRepository;
import com.example.appsaudebackend.shared.exception.ConflitoException;
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
    @Mock PacienteRepository pacienteRepository;
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
        verify(pacienteRepository).findByEmailIgnoreCase("ana@example.com");
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
