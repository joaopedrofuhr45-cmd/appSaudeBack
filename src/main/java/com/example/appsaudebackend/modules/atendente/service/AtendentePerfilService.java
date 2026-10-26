package com.example.appsaudebackend.modules.atendente.service;

import com.example.appsaudebackend.modules.atendente.dto.request.AtualizarAtendenteRequestDto;
import com.example.appsaudebackend.modules.atendente.dto.request.AlterarSenhaAtendenteRequestDto;
import com.example.appsaudebackend.modules.atendente.dto.response.AtendentePerfilResponseDto;
import com.example.appsaudebackend.modules.atendente.persistencia.*;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.auth.service.AuthAccountService;
import com.example.appsaudebackend.shared.exception.RecursoNaoEncontradoException;
import com.example.appsaudebackend.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.appsaudebackend.modules.perfil.service.FotoPerfilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtendentePerfilService {
    private final AtendenteRepository repo;
    private final UsuarioAuthRepository authRepository;
    private final AuthAccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final FotoPerfilService fotoService;

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
        return new AtendentePerfilResponseDto(a.getNome(), a.getEmail(), a.getTelefone(), a.getSetor(), fotoService.fotoUrl(a.getUsuarioAuth()));
    }
}
