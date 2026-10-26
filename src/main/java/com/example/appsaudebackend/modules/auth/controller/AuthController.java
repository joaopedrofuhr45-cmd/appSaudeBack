package com.example.appsaudebackend.modules.auth.controller;

import com.example.appsaudebackend.modules.auth.dto.request.CadastroPacienteDto;
import com.example.appsaudebackend.modules.auth.dto.request.LoginRequestDto;
import com.example.appsaudebackend.modules.auth.dto.request.GoogleLoginRequestDto;
import com.example.appsaudebackend.modules.auth.dto.response.MeResponseDto;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.service.AuthService;
import com.example.appsaudebackend.modules.auth.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final LoginAttemptService loginAttempts;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto request,
                                      HttpServletRequest httpRequest, HttpServletResponse response) {
        String ip = httpRequest.getRemoteAddr();
        String email = request.getEmail();
        loginAttempts.verificarBloqueio(ip, email);
        String token;
        try {
            token = authService.login(email, request.getSenha());
        } catch (AuthenticationException e) {
            loginAttempts.registrarFalha(ip, email);
            throw e;
        }
        loginAttempts.registrarSucesso(ip, email);
        adicionarCookie(response, token, Duration.ofMillis(expiration));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public ResponseEntity<Void> loginComGoogle(@Valid @RequestBody GoogleLoginRequestDto request,
                                               HttpServletResponse response) {
        String token = authService.loginComGoogle(request.credential());
        adicionarCookie(response, token, Duration.ofMillis(expiration));
        return ResponseEntity.ok().build();
    }

    private void adicionarCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true).secure(cookieSecure).path("/")
                .maxAge(maxAge).sameSite(cookieSameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true).secure(cookieSecure).path("/")
                .maxAge(Duration.ZERO).sameSite(cookieSameSite).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    /** Entrega o token CSRF (o cookie XSRF-TOKEN também é emitido). O front manda o valor no header X-XSRF-TOKEN. */
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(Map.of("headerName", token.getHeaderName(), "token", token.getToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> me(@AuthenticationPrincipal UsuarioAuth usuario) {
        return ResponseEntity.ok(new MeResponseDto(usuario.getEmail(), usuario.getRole().name()));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody CadastroPacienteDto dto) {
        authService.cadastrarPaciente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
