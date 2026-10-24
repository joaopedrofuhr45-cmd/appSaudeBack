package com.example.appsaudebackend.Modules.Paciente.Controller;

import com.example.appsaudebackend.Modules.Paciente.Dto.Response.PacienteOpcaoResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Service.PacienteBuscaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteBuscaController {
    private final PacienteBuscaService service;

    @GetMapping
    public ResponseEntity<List<PacienteOpcaoResponseDto>> buscar(
            @RequestParam(defaultValue = "") String nome) {
        return ResponseEntity.ok(service.buscar(nome));
    }
}