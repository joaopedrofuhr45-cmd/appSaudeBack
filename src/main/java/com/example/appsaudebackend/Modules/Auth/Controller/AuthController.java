package com.example.appsaudebackend.Modules.Auth.Controller;

import com.example.appsaudebackend.Modules.Auth.Dto.Request.LoginRequestDto;
import com.example.appsaudebackend.Modules.Auth.Service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration}")
    private long expiration;

    // Agora vem do .env / variável de ambiente (app.cookie.secure)
    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        String token = authService.login(request.getCpf(), request.getSenha());

        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(cookieSecure) // true em produção via COOKIE_SECURE=true
                .path("/")
                .maxAge(Duration.ofMillis(expiration))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }
}