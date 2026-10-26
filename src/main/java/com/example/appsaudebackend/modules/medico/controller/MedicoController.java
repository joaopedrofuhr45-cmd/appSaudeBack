package com.example.appsaudebackend.modules.medico.controller;

import com.example.appsaudebackend.modules.medico.dto.response.MedicoResponseDto;
import com.example.appsaudebackend.modules.medico.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MedicoController {
    private final MedicoService medicoService;

    @GetMapping("/medicos")
    public ResponseEntity<List<MedicoResponseDto>> listar(
            @RequestParam(required = false) String especialidade) {
        return ResponseEntity.ok(medicoService.listar(especialidade));
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<String>> listarEspecialidades() {
        return ResponseEntity.ok(medicoService.listarEspecialidades());
    }
}
