package com.example.appsaudebackend.Modules.Medico.Controller;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.ConsultaResponseDto;
import com.example.appsaudebackend.Modules.Medico.Service.MedicoConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/medicos/me/consultas")
@RequiredArgsConstructor
public class MedicoConsultaController {
    private final MedicoConsultaService service;

    @GetMapping
    public ResponseEntity<List<ConsultaResponseDto>> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(service.listarAgenda(data, solicitante));
    }
}
