package com.example.appsaudebackend.Modules.Auth.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService
        implements UserDetailsService {

    private final AuthAccountService accountService;
    private final com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository usuarioAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return accountService.encontrarPorEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não encontrado."));
    }

    public UserDetails loadById(Long id) {
        return usuarioAuthRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Conta autenticada não encontrada."));
    }
}
