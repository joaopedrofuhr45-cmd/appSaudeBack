package com.example.appsaudebackend.Modules.Medico.Controller;

import com.example.appsaudebackend.Modules.Medico.Dto.Response.MedicoResponseDto;
import com.example.appsaudebackend.Modules.Medico.Service.MedicoService;
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
