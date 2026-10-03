package com.example.appsaudebackend.Modules.Atendente.Service;

import com.example.appsaudebackend.Modules.Atendente.Dto.Request.AtualizarAtendenteRequestDto;
import com.example.appsaudebackend.Modules.Atendente.Dto.Request.AlterarSenhaAtendenteRequestDto;
import com.example.appsaudebackend.Modules.Atendente.Dto.Response.AtendentePerfilResponseDto;
import com.example.appsaudebackend.Modules.Atendente.Pesistencia.*;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtendentePerfilService {
    private final AtendenteRepository repo;
    private final UsuarioAuthRepository authRepository;
    private final AuthAccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AtendentePerfilResponseDto obter(String email) {
        return toResponse(find(email));
    }

    @Transactional
    public AtendentePerfilResponseDto atualizar(String email, AtualizarAtendenteRequestDto dto) {
        AtendenteModel a = find(email);
        String novoEmail = accountService.normalizar(dto.email());
        accountService.garantirEmailDisponivel(novoEmail, a.getUsuarioAuth().getId());
        a.setNome(dto.nome());
        a.setEmail(novoEmail);
        a.setTelefone(dto.telefone());
        a.getUsuarioAuth().setEmail(novoEmail);
        authRepository.save(a.getUsuarioAuth());
        return toResponse(repo.save(a));
    }

    @Transactional
    public void alterarSenha(String email, AlterarSenhaAtendenteRequestDto dto) {
        AtendenteModel atendente = find(email);
        var auth = atendente.getUsuarioAuth();
        if (!passwordEncoder.matches(dto.senhaAtual(), auth.getSenha())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        if (passwordEncoder.matches(dto.novaSenha(), auth.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        }
        auth.setSenha(passwordEncoder.encode(dto.novaSenha()));
        authRepository.save(auth);
    }

    private AtendenteModel find(String email) {
        return repo.findByUsuarioAuthEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Atendente não encontrado."));
    }

    private AtendentePerfilResponseDto toResponse(AtendenteModel a) {
        return new AtendentePerfilResponseDto(a.getNome(), a.getEmail(), a.getTelefone(), a.getSetor());
    }
}
