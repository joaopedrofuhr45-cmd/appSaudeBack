package com.example.appsaudebackend.Modules.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String login(String cpf, String senha) {

        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(cpf, senha);

        Authentication authentication =
                authenticationManager.authenticate(credentials);

        UsuarioAuth usuarioAuth =
                (UsuarioAuth) authentication.getPrincipal();

        return jwtService.generateToken(usuarioAuth);
    }
}
