package com.example.appsaudebackend.modules.auth.service;
import com.example.appsaudebackend.modules.auth.dto.request.CadastroPacienteDto;
import com.example.appsaudebackend.modules.auth.model.Role;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.repository.UsuarioAuthRepository;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteRepository;
import com.example.appsaudebackend.shared.exception.ConflitoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioAuthRepository usuarioAuthRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAccountService accountService;
    private final GoogleIdentityService googleIdentityService;

    public String login(String email, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(accountService.normalizar(email), senha)
        );
        UsuarioAuth usuarioAuth = (UsuarioAuth) authentication.getPrincipal();
        return jwtService.generateToken(usuarioAuth);
    }

    public String loginComGoogle(String credential) {
        return jwtService.generateToken(googleIdentityService.autenticar(credential));
    }

    @Transactional
    public void cadastrarPaciente(CadastroPacienteDto dto) {
        String email = accountService.normalizar(dto.getEmail());
        accountService.garantirEmailDisponivel(email, null);
        UsuarioAuth usuarioAuth = new UsuarioAuth();
        usuarioAuth.setInternalKey(UUID.randomUUID().toString());
        usuarioAuth.setEmail(email);
        usuarioAuth.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuarioAuth.setRole(Role.USUARIO);
        usuarioAuthRepository.save(usuarioAuth);

        PacienteModel usuario = new PacienteModel();
        usuario.setNome(dto.getNome());
        usuario.setEmail(email);
        usuario.setUsuarioAuth(usuarioAuth);
        pacienteRepository.save(usuario);
    }
}
