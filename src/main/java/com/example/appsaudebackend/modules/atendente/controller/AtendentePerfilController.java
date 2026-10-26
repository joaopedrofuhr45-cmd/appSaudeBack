package com.example.appsaudebackend.modules.atendente.controller;

import com.example.appsaudebackend.modules.atendente.dto.request.AtualizarAtendenteRequestDto;
import com.example.appsaudebackend.modules.atendente.dto.request.AlterarSenhaAtendenteRequestDto;
import com.example.appsaudebackend.modules.atendente.dto.response.AtendentePerfilResponseDto;
import com.example.appsaudebackend.modules.atendente.service.AtendentePerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atendentes/me")
@RequiredArgsConstructor
public class AtendentePerfilController {
    private final AtendentePerfilService service;

    @GetMapping
    public ResponseEntity<AtendentePerfilResponseDto> obter(Authentication a) {
        return ResponseEntity.ok(service.obter(a.getName()));
    }

    @PutMapping
    public ResponseEntity<AtendentePerfilResponseDto> atualizar(Authentication a, @Valid @RequestBody AtualizarAtendenteRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(a.getName(), dto));
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(Authentication a, @Valid @RequestBody AlterarSenhaAtendenteRequestDto dto) {
        service.alterarSenha(a.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
