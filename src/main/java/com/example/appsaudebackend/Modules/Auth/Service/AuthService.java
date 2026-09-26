package com.example.appsaudebackend.Modules.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Dto.Request.CadastroPacienteDto;
import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UsuarioAuthRepository usuarioAuthRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(String cpf, String senha) {

        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(cpf, senha);

        Authentication authentication =
                authenticationManager.authenticate(credentials);

        UsuarioAuth usuarioAuth =
                (UsuarioAuth) authentication.getPrincipal();

        return jwtService.generateToken(usuarioAuth);
    }

    public void cadastrarPaciente(CadastroPacienteDto dto) {
        if (usuarioAuthRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        UsuarioAuth usuarioAuth = new UsuarioAuth();
        usuarioAuth.setCpf(dto.getCpf());
        usuarioAuth.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuarioAuth.setRole(Role.USUARIO);
        usuarioAuthRepository.save(usuarioAuth);

        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setUsuarioAuth(usuarioAuth);
        usuarioRepository.save(usuario);
    }
}
