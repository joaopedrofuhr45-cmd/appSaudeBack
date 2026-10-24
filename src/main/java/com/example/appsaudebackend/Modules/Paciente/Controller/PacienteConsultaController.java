package com.example.appsaudebackend.Modules.Paciente.Controller;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Paciente.Dto.Response.HistoricoPacienteResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Dto.Response.PacienteConsultaResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Service.PacienteConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/pacientes/me/consultas")
@RequiredArgsConstructor
public class PacienteConsultaController {
    private final PacienteConsultaService service;

    @GetMapping
    public ResponseEntity<List<PacienteConsultaResponseDto>> minhasConsultas(
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(service.listarConsultas(solicitante));
    }

    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoPacienteResponseDto>> historico(
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(service.listarHistorico(solicitante));
    }
}
