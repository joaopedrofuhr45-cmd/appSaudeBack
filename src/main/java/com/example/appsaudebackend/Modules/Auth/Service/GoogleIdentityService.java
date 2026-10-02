package com.example.appsaudebackend.Modules.Auth.Service;

import com.example.appsaudebackend.Modules.Auth.Model.Role;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Auth.Repository.UsuarioAuthRepository;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import com.example.appsaudebackend.Shared.Exception.RegraNegocioException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleIdentityService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthAccountService accountService;
    private final UsuarioAuthRepository authRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.google.client-id:}")
    private String clientId;

    @Transactional
    public UsuarioAuth autenticar(String credential) {
        GoogleIdToken.Payload payload = verificar(credential);
        String email = accountService.normalizar(payload.getEmail());
        String googleSub = payload.getSubject();
        String nome = payload.get("name") == null ? "" : payload.get("name").toString().trim();

        if (email.isBlank() || nome.isBlank() || !Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new RegraNegocioException("A conta Google precisa ter nome e e-mail verificado.");
        }

        UsuarioAuth conta = authRepository.findByGoogleSub(googleSub).orElse(null);
        if (conta != null) return conta;

        conta = accountService.encontrarPorEmail(email).orElse(null);
        if (conta != null) {
            if (conta.getGoogleSub() != null && !conta.getGoogleSub().equals(googleSub)) {
                throw new RegraNegocioException("Este e-mail já está vinculado a outra conta Google.");
            }
            conta.setGoogleSub(googleSub);
            conta.setEmail(email);
            return authRepository.save(conta);
        }

        accountService.garantirEmailDisponivel(email, null);
        conta = new UsuarioAuth();
        conta.setInternalKey(UUID.randomUUID().toString());
        conta.setEmail(email);
        conta.setGoogleSub(googleSub);
        conta.setSenha(passwordEncoder.encode(senhaAleatoria()));
        conta.setRole(Role.USUARIO);
        conta = authRepository.save(conta);

        UsuarioModel paciente = new UsuarioModel();
        paciente.setNome(nome);
        paciente.setEmail(email);
        paciente.setTelefone(null);
        paciente.setUsuarioAuth(conta);
        usuarioRepository.save(paciente);
        return conta;
    }

    private GoogleIdToken.Payload verificar(String credential) {
        if (clientId == null || clientId.isBlank()) {
            throw new RegraNegocioException("O login Google ainda não foi configurado no servidor.");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(List.of(clientId))
                    .build();
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) throw new RegraNegocioException("Credencial Google inválida ou expirada.");
            return token.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new RegraNegocioException("Não foi possível validar a credencial Google.");
        }
    }

    private String senhaAleatoria() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
