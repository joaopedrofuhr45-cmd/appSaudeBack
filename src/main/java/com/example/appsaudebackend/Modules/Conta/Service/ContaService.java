package com.example.appsaudebackend.Modules.Conta.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Conta.Dto.Request.AlterarSenhaRequestDto;
import com.example.appsaudebackend.Shared.Exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContaService {
    private final UsuarioAuthRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public void alterarSenha(String email, AlterarSenhaRequestDto dto) {
        UsuarioAuth a = repo.findByEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
        if (!encoder.matches(dto.senhaAtual(), a.getSenha()))
            throw new RegraNegocioException("A senha atual está incorreta.");
        if (encoder.matches(dto.novaSenha(), a.getSenha()))
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        a.setSenha(encoder.encode(dto.novaSenha()));
        repo.save(a);
    }
}
