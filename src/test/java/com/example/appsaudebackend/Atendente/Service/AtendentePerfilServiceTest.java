package com.example.appsaudebackend.Atendente.Service;

import com.example.appsaudebackend.Modules.Atendente.Dto.Request.AlterarSenhaAtendenteRequestDto;
import com.example.appsaudebackend.Modules.Atendente.Dto.Request.AtualizarAtendenteRequestDto;
import com.example.appsaudebackend.Modules.Atendente.Pesistencia.AtendenteModel;
import com.example.appsaudebackend.Modules.Atendente.Pesistencia.AtendenteRepository;
import com.example.appsaudebackend.Modules.Atendente.Service.AtendentePerfilService;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
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
class AtendentePerfilServiceTest {
    @Mock AtendenteRepository repository;
    @Mock UsuarioAuthRepository authRepository;
    @Mock AuthAccountService accountService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AtendentePerfilService service;

    private AtendenteModel atendente() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(30L);
        auth.setEmail("atendente@example.com");
        auth.setSenha("hash-antiga");
        AtendenteModel atendente = new AtendenteModel();
        atendente.setId(3L);
        atendente.setNome("Joana");
        atendente.setEmail("atendente@example.com");
        atendente.setTelefone("111");
        atendente.setSetor("Recepção");
        atendente.setUsuarioAuth(auth);
        return atendente;
    }

    @Test
    void obterRetornaPerfilDoAtendente() {
        when(repository.findByUsuarioAuthEmailIgnoreCase("atendente@example.com"))
                .thenReturn(Optional.of(atendente()));
        var perfil = service.obter("atendente@example.com");
        assertAll(
                () -> assertEquals("Joana", perfil.nome()),
                () -> assertEquals("Recepção", perfil.setor()),
                () -> assertEquals("atendente@example.com", perfil.email())
        );
    }

    @Test
    void atualizarSincronizaEmailNoPerfilEContaAutenticada() {
        AtendenteModel atendente = atendente();
        when(repository.findByUsuarioAuthEmailIgnoreCase("atendente@example.com"))
                .thenReturn(Optional.of(atendente));
        when(accountService.normalizar(" NOVO@example.com ")).thenReturn("novo@example.com");
        when(repository.save(atendente)).thenReturn(atendente);

        var perfil = service.atualizar("atendente@example.com",
                new AtualizarAtendenteRequestDto("Joana Silva", " NOVO@example.com ", "222"));

        assertAll(
                () -> assertEquals("Joana Silva", perfil.nome()),
                () -> assertEquals("novo@example.com", perfil.email()),
                () -> assertEquals("222", perfil.telefone()),
                () -> assertEquals("novo@example.com", atendente.getUsuarioAuth().getEmail())
        );
        verify(accountService).garantirEmailDisponivel("novo@example.com", 30L);
        verify(authRepository).save(atendente.getUsuarioAuth());
        verify(repository).save(atendente);
    }

    @Test
    void alterarSenhaConfereSenhaAtualAntesDeSalvar() {
        AtendenteModel atendente = atendente();
        when(repository.findByUsuarioAuthEmailIgnoreCase("atendente@example.com"))
                .thenReturn(Optional.of(atendente));
        when(passwordEncoder.matches("atual", "hash-antiga")).thenReturn(true);
        when(passwordEncoder.matches("nova", "hash-antiga")).thenReturn(false);
        when(passwordEncoder.encode("nova")).thenReturn("hash-nova");

        service.alterarSenha("atendente@example.com", new AlterarSenhaAtendenteRequestDto("atual", "nova"));

        assertEquals("hash-nova", atendente.getUsuarioAuth().getSenha());
        verify(authRepository).save(atendente.getUsuarioAuth());
    }

    @Test
    void senhaAtualIncorretaNaoSalva() {
        AtendenteModel atendente = atendente();
        when(repository.findByUsuarioAuthEmailIgnoreCase("atendente@example.com"))
                .thenReturn(Optional.of(atendente));
        when(passwordEncoder.matches("errada", "hash-antiga")).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> service.alterarSenha("atendente@example.com",
                new AlterarSenhaAtendenteRequestDto("errada", "nova")));
        verify(authRepository, never()).save(any());
    }

    @Test
    void perfilAusenteLancaErro() {
        when(repository.findByUsuarioAuthEmailIgnoreCase("ausente@example.com")).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter("ausente@example.com"));
    }
}
