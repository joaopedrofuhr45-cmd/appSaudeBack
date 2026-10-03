package com.example.appsaudebackend.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Dto.Request.CadastroPacienteDto;
import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import com.example.appsaudebackend.Modules.Auth.Service.GoogleIdentityService;
import com.example.appsaudebackend.Modules.Auth.Service.JwtService;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
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
    @Mock UsuarioRepository usuarioRepository;
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
        ArgumentCaptor<UsuarioModel> userCaptor = ArgumentCaptor.forClass(UsuarioModel.class);
        verify(usuarioRepository).save(userCaptor.capture());
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
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(passwordEncoder);
    }
}
