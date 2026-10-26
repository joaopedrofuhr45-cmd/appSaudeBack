package com.example.appsaudebackend.modules.paciente.controller;

import com.example.appsaudebackend.modules.paciente.dto.request.*;
import com.example.appsaudebackend.modules.paciente.dto.response.PerfilResponseDto;
import com.example.appsaudebackend.modules.paciente.service.PacientePerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes/me")
@RequiredArgsConstructor
public class PacientePerfilController {
    private final PacientePerfilService service;

    @GetMapping
    public ResponseEntity<PerfilResponseDto> obter(Authentication a) {
        return ResponseEntity.ok(service.obter(a.getName()));
    }

    @PutMapping
    public ResponseEntity<PerfilResponseDto> atualizar(Authentication a, @Valid @RequestBody AtualizarPerfilRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(a.getName(), dto));
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> senha(Authentication a, @Valid @RequestBody AlterarSenhaRequestDto dto) {
        service.alterarSenha(a.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}