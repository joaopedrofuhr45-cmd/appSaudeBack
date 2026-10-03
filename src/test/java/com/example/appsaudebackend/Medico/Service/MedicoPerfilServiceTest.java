package com.example.appsaudebackend.Medico.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Medico.Dto.Request.AlterarSenhaMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Dto.Request.AtualizarMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Modules.Medico.Service.MedicoPerfilService;
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
class MedicoPerfilServiceTest {
    @Mock MedicoRepository medicoRepository;
    @Mock UsuarioAuthRepository authRepository;
    @Mock AuthAccountService accountService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks MedicoPerfilService service;

    private MedicoModel medico() {
        UsuarioAuth auth = new UsuarioAuth();
        auth.setId(20L);
        auth.setEmail("medico@example.com");
        auth.setSenha("hash-antiga");
        MedicoModel medico = new MedicoModel();
        medico.setId(2L);
        medico.setNome("Dra. Ana");
        medico.setEmail("medico@example.com");
        medico.setTelefone("111");
        medico.setCrm("CRM-123");
        medico.setEspecialidade("Cardiologia");
        medico.setUsuarioAuth(auth);
        return medico;
    }

    @Test
    void obterRetornaPerfilMedico() {
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("medico@example.com"))
                .thenReturn(Optional.of(medico()));

        var perfil = service.obter("medico@example.com");

        assertAll(
                () -> assertEquals("Dra. Ana", perfil.nome()),
                () -> assertEquals("CRM-123", perfil.crm()),
                () -> assertEquals("Cardiologia", perfil.especialidade())
        );
    }

    @Test
    void atualizarNormalizaEmailESincronizaConta() {
        MedicoModel medico = medico();
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("medico@example.com"))
                .thenReturn(Optional.of(medico));
        when(accountService.normalizar(" NOVO@example.com ")).thenReturn("novo@example.com");
        when(medicoRepository.save(medico)).thenReturn(medico);

        var perfil = service.atualizar("medico@example.com",
                new AtualizarMedicoRequestDto("Dr. Novo", " NOVO@example.com ", "222"));

        assertAll(
                () -> assertEquals("Dr. Novo", perfil.nome()),
                () -> assertEquals("novo@example.com", perfil.email()),
                () -> assertEquals("222", perfil.telefone()),
                () -> assertEquals("novo@example.com", medico.getUsuarioAuth().getEmail())
        );
        verify(accountService).garantirEmailDisponivel("novo@example.com", 20L);
        verify(authRepository).save(medico.getUsuarioAuth());
        verify(medicoRepository).save(medico);
    }

    @Test
    void emailDuplicadoNaoAtualizaPerfil() {
        MedicoModel medico = medico();
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("medico@example.com"))
                .thenReturn(Optional.of(medico));
        when(accountService.normalizar("outro@example.com")).thenReturn("outro@example.com");
        doThrow(new ConflitoException("E-mail já cadastrado."))
                .when(accountService).garantirEmailDisponivel("outro@example.com", 20L);

        assertThrows(ConflitoException.class, () -> service.atualizar("medico@example.com",
                new AtualizarMedicoRequestDto("Dra. Ana", "outro@example.com", "222")));
        verify(authRepository, never()).save(any());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void alterarSenhaValidaSenhaAtualEAtualizaHash() {
        MedicoModel medico = medico();
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("medico@example.com"))
                .thenReturn(Optional.of(medico));
        when(passwordEncoder.matches("atual", "hash-antiga")).thenReturn(true);
        when(passwordEncoder.matches("nova", "hash-antiga")).thenReturn(false);
        when(passwordEncoder.encode("nova")).thenReturn("hash-nova");

        service.alterarSenha("medico@example.com", new AlterarSenhaMedicoRequestDto("atual", "nova"));

        assertEquals("hash-nova", medico.getUsuarioAuth().getSenha());
        verify(authRepository).save(medico.getUsuarioAuth());
    }

    @Test
    void senhaAtualIncorretaNaoSalva() {
        MedicoModel medico = medico();
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("medico@example.com"))
                .thenReturn(Optional.of(medico));
        when(passwordEncoder.matches("errada", "hash-antiga")).thenReturn(false);

        assertThrows(RegraNegocioException.class, () -> service.alterarSenha("medico@example.com",
                new AlterarSenhaMedicoRequestDto("errada", "nova")));
        verify(authRepository, never()).save(any());
    }

    @Test
    void obterFalhaQuandoMedicoNaoExiste() {
        when(medicoRepository.findByUsuarioAuthEmailIgnoreCase("ausente@example.com"))
                .thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.obter("ausente@example.com"));
    }
}
