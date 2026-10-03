package com.example.appsaudebackend.Usuarios.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Request.AlterarSenhaRequestDto;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Request.AtualizarPerfilRequestDto;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Modules.Usuarios.Service.PerfilService;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock UsuarioAuthRepository authRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthAccountService accountService;
    @InjectMocks PerfilService service;

    private UsuarioModel paciente() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(10L);
        auth.setEmail("ana@example.com");
        auth.setSenha("hash-antiga");
        UsuarioModel paciente = new UsuarioModel();
        paciente.setId(1L);
        paciente.setNome("Ana");
        paciente.setEmail("ana@example.com");
        paciente.setTelefone("111");
        paciente.setUsuarioAuth(auth);
        return paciente;
    }

    @Test
    void obterRetornaPerfilAssociadoAoEmailAutenticado() {
        when(usuarioRepository.findByUsuarioAuthEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(paciente()));

        var perfil = service.obter("ana@example.com");

        assertAll(
                () -> assertEquals("Ana", perfil.nome()),
                () -> assertEquals("ana@example.com", perfil.email()),
                () -> assertEquals("111", perfil.telefone())
        );
    }

    @Test
    void atualizarNormalizaEmailEAtualizaContaEPerfil() {
        UsuarioModel paciente = paciente();
        when(usuarioRepository.findByUsuarioAuthEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(paciente));
        when(accountService.normalizar(" ANA+novo@Example.com ")).thenReturn("ana+novo@example.com");
        when(usuarioRepository.save(paciente)).thenReturn(paciente);

        var resposta = service.atualizar("ana@example.com",
                new AtualizarPerfilRequestDto("Ana Silva", " ANA+novo@Example.com ", "222"));

        assertAll(
                () -> assertEquals("Ana Silva", resposta.nome()),
                () -> assertEquals("ana+novo@example.com", resposta.email()),
                () -> assertEquals("222", resposta.telefone()),
                () -> assertEquals("ana+novo@example.com", paciente.getUsuarioAuth().getEmail())
        );
        verify(accountService).garantirEmailDisponivel("ana+novo@example.com", 10L);
        verify(authRepository).save(paciente.getUsuarioAuth());
        verify(usuarioRepository).save(paciente);
    }

    @Test
    void emailEmUsoImpedeAtualizacaoSemPersistir() {
        UsuarioModel paciente = paciente();
        when(usuarioRepository.findByUsuarioAuthEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(paciente));
        when(accountService.normalizar("outro@example.com")).thenReturn("outro@example.com");
        doThrow(new ConflitoException("E-mail já cadastrado."))
                .when(accountService).garantirEmailDisponivel("outro@example.com", 10L);

        assertThrows(ConflitoException.class, () -> service.atualizar("ana@example.com",
                new AtualizarPerfilRequestDto("Ana", "outro@example.com", "222")));

        verify(authRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void alterarSenhaVerificaAtualECodificaNovaSenha() {
        UsuarioAuth auth = paciente().getUsuarioAuth();
        when(accountService.encontrarPorEmail("ana@example.com")).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("atual", "hash-antiga")).thenReturn(true);
        when(passwordEncoder.matches("nova", "hash-antiga")).thenReturn(false);
        when(passwordEncoder.encode("nova")).thenReturn("hash-nova");

        service.alterarSenha("ana@example.com", new AlterarSenhaRequestDto("atual", "nova"));

        assertEquals("hash-nova", auth.getSenha());
        verify(authRepository).save(auth);
    }

    @Test
    void senhaAtualIncorretaNaoPersiste() {
        UsuarioAuth auth = paciente().getUsuarioAuth();
        when(accountService.encontrarPorEmail("ana@example.com")).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("incorreta", "hash-antiga")).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> service.alterarSenha("ana@example.com",
                new AlterarSenhaRequestDto("incorreta", "nova")));
        verify(authRepository, never()).save(any());
    }

    @Test
    void obterFalhaQuandoPacienteNaoExiste() {
        when(usuarioRepository.findByUsuarioAuthEmailIgnoreCase("inexistente@example.com"))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter("inexistente@example.com"));
    }
}
