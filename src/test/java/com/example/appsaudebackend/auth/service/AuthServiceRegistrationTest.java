package com.example.appsaudebackend.auth.service;

import com.example.appsaudebackend.modules.auth.dto.request.CadastroPacienteDto;
import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.auth.service.AuthAccountService;
import com.example.appsaudebackend.modules.auth.service.AuthService;
import com.example.appsaudebackend.modules.auth.service.GoogleIdentityService;
import com.example.appsaudebackend.modules.auth.service.JwtService;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteRepository;
import com.example.appsaudebackend.shared.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UsuarioAuthRepository authRepository;
    @Mock PacienteRepository pacienteRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthAccountService accountService;
    @Mock GoogleIdentityService googleIdentityService;
    @InjectMocks AuthService service;

    private CadastroPacienteDto cadastro(String email) {
        CadastroPacienteDto dto = new CadastroPacienteDto();
        dto.setNome("Ana Silva");
        dto.setEmail(email);
        dto.setSenha("senha123");
        return dto;
    }

    @Test
    void cadastraPacienteComEmailNormalizadoESenhaCodificada() {
        CadastroPacienteDto dto = cadastro(" Ana@Example.com ");
        when(accountService.normalizar(dto.getEmail())).thenReturn("ana@example.com");
        when(passwordEncoder.encode("senha123")).thenReturn("hash-da-senha");

        service.cadastrarPaciente(dto);

        ArgumentCaptor<UsuarioAuth> authCaptor = ArgumentCaptor.forClass(UsuarioAuth.class);
        verify(authRepository).save(authCaptor.capture());
        UsuarioAuth auth = authCaptor.getValue();
        assertAll(
                () -> assertEquals("ana@example.com", auth.getEmail()),
                () -> assertEquals("hash-da-senha", auth.getSenha()),
                () -> assertEquals(Role.USUARIO, auth.getRole()),
                () -> assertNotNull(auth.getInternalKey())
        );
        ArgumentCaptor<PacienteModel> userCaptor = ArgumentCaptor.forClass(PacienteModel.class);
        verify(pacienteRepository).save(userCaptor.capture());
        assertEquals("Ana Silva", userCaptor.getValue().getNome());
        assertEquals("ana@example.com", userCaptor.getValue().getEmail());
        assertEquals(auth, userCaptor.getValue().getUsuarioAuth());
        verify(accountService).garantirEmailDisponivel("ana@example.com", null);
    }

    @Test
    void emailDuplicadoImpedeCadastroSemPersistir() {
        CadastroPacienteDto dto = cadastro("ana@example.com");
        when(accountService.normalizar(dto.getEmail())).thenReturn("ana@example.com");
        doThrow(new ConflitoException("E-mail já cadastrado."))
                .when(accountService).garantirEmailDisponivel("ana@example.com", null);

        assertThrows(ConflitoException.class, () -> service.cadastrarPaciente(dto));
        verify(authRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(pacienteRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(passwordEncoder);
    }
}
