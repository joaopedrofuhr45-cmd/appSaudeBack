package com.example.appsaudebackend.Modules.Auth.Service;
import com.example.appsaudebackend.Modules.Auth.Dto.Request.CadastroPacienteDto;
import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.ConflitoException;
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
    private final UsuarioRepository usuarioRepository;
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

        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(dto.getNome());
        usuario.setEmail(email);
        usuario.setUsuarioAuth(usuarioAuth);
        usuarioRepository.save(usuario);
    }
}
