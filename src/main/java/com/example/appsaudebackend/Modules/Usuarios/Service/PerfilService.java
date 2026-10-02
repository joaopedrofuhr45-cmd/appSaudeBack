package com.example.appsaudebackend.Modules.Usuarios.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Auth.Service.AuthAccountService;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Request.*;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Response.PerfilResponseDto;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.*;
import com.example.appsaudebackend.Shared.Exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountService accountService;

    @Transactional(readOnly = true)
    public PerfilResponseDto obter(String email) {
        return toResponse(findUsuario(email));
    }

    @Transactional
    public PerfilResponseDto atualizar(String email, AtualizarPerfilRequestDto dto) {
        UsuarioModel u = findUsuario(email);
        UsuarioAuth auth = u.getUsuarioAuth();
        String novoEmail = accountService.normalizar(dto.email());
        accountService.garantirEmailDisponivel(novoEmail, auth.getId());
        u.setNome(dto.nome());
        u.setEmail(novoEmail);
        u.setTelefone(dto.telefone());
        auth.setEmail(novoEmail);
        authRepository.save(auth);
        return toResponse(usuarioRepository.save(u));
    }

    @Transactional
    public void alterarSenha(String email, AlterarSenhaRequestDto dto) {
        UsuarioAuth a = findAuth(email);
        if (!passwordEncoder.matches(dto.senhaAtual(), a.getSenha()))
            throw new RegraNegocioException("A senha atual está incorreta.");
        if (passwordEncoder.matches(dto.novaSenha(), a.getSenha()))
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        a.setSenha(passwordEncoder.encode(dto.novaSenha()));
        authRepository.save(a);
    }

    private UsuarioModel findUsuario(String email) {
        return usuarioRepository.findByUsuarioAuthEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    private UsuarioAuth findAuth(String email) {
        return accountService.encontrarPorEmail(email).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }

    private PerfilResponseDto toResponse(UsuarioModel u) {
        return new PerfilResponseDto(u.getNome(), u.getEmail(), u.getTelefone());
    }
}
