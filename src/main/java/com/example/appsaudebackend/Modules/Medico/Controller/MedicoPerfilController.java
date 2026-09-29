package com.example.appsaudebackend.Modules.Medico.Controller;

import com.example.appsaudebackend.Modules.Medico.Dto.Request.AtualizarMedicoRequestDto;
import com.example.appsaudebackend.Modules.Medico.Dto.Response.MedicoPerfilResponseDto;
import com.example.appsaudebackend.Modules.Medico.Service.MedicoPerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicos/me")
@RequiredArgsConstructor
public class MedicoPerfilController {
    private final MedicoPerfilService service;

    @GetMapping
    public ResponseEntity<MedicoPerfilResponseDto> obter(Authentication a) {
        return ResponseEntity.ok(service.obter(a.getName()));
    }

    @PutMapping
    public ResponseEntity<MedicoPerfilResponseDto> atualizar(Authentication a, @Valid @RequestBody AtualizarMedicoRequestDto dto) {
        return ResponseEntity.ok(service.atualizar(a.getName(), dto));
    }
}