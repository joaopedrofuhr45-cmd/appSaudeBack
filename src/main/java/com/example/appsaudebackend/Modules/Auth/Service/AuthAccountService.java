package com.example.appsaudebackend.Modules.Auth.Service;

import com.example.appsaudebackend.Modules.Atendente.Pesistencia.AtendenteRepository;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthAccountService {
    private final UsuarioAuthRepository authRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final AtendenteRepository atendenteRepository;

    @Transactional
    public Optional<UsuarioAuth> encontrarPorEmail(String email) {
        String normalizado = normalizar(email);
        List<UsuarioAuth> encontrados = new ArrayList<>();
        authRepository.findByEmailIgnoreCase(normalizado).ifPresent(encontrados::add);
        usuarioRepository.findByEmailIgnoreCase(normalizado).map(u -> u.getUsuarioAuth()).ifPresent(encontrados::add);
        medicoRepository.findByEmailIgnoreCase(normalizado).map(m -> m.getUsuarioAuth()).ifPresent(encontrados::add);
        atendenteRepository.findByEmailIgnoreCase(normalizado).map(a -> a.getUsuarioAuth()).ifPresent(encontrados::add);

        List<UsuarioAuth> unicos = encontrados.stream().distinct().toList();
        if (unicos.size() > 1) {
            throw new UsernameNotFoundException("O e-mail está associado a mais de uma conta.");
        }
        if (unicos.isEmpty()) return Optional.empty();

        UsuarioAuth conta = unicos.get(0);
        if (!normalizado.equals(conta.getEmail())) {
            conta.setEmail(normalizado);
            conta = authRepository.save(conta);
        }
        return Optional.of(conta);
    }

    @Transactional(readOnly = true)
    public void garantirEmailDisponivel(String email, Long ignorarAuthId) {
        String normalizado = normalizar(email);
        boolean usadoPorOutro = authRepository.findByEmailIgnoreCase(normalizado)
                .filter(auth -> !auth.getId().equals(ignorarAuthId)).isPresent()
                || usuarioRepository.findByEmailIgnoreCase(normalizado)
                    .filter(u -> !u.getUsuarioAuth().getId().equals(ignorarAuthId)).isPresent()
                || medicoRepository.findByEmailIgnoreCase(normalizado)
                    .filter(m -> !m.getUsuarioAuth().getId().equals(ignorarAuthId)).isPresent()
                || atendenteRepository.findByEmailIgnoreCase(normalizado)
                    .filter(a -> !a.getUsuarioAuth().getId().equals(ignorarAuthId)).isPresent();
        if (usadoPorOutro) throw new ConflitoException("E-mail já cadastrado.");
    }

    public String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
