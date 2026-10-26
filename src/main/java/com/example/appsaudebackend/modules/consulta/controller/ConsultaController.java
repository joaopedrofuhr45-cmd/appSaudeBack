package com.example.appsaudebackend.modules.consulta.controller;

import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.consulta.dto.request.ConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.dto.request.FinalizarConsultaRequestDto;
import com.example.appsaudebackend.modules.consulta.dto.response.ConsultaDetalheResponseDto;
import com.example.appsaudebackend.modules.consulta.dto.response.ConsultaResponseDto;
import com.example.appsaudebackend.modules.consulta.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
public class ConsultaController {
    private final ConsultaService consultaService;

    /** Agenda geral do dia (atendente). Médico: GET /medicos/me/consultas. Paciente: GET /pacientes/me/consultas. */
    @GetMapping
    public ResponseEntity<List<ConsultaResponseDto>> listarPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(consultaService.listarPorData(data, solicitante));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDetalheResponseDto> obter(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(consultaService.obter(id, solicitante));
    }

    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody ConsultaRequestDto dto,
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        consultaService.criar(dto, solicitante);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ConsultaDetalheResponseDto> finalizar(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarConsultaRequestDto dto,
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(consultaService.finalizar(id, dto, solicitante));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaDetalheResponseDto> cancelar(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioAuth solicitante) {
        return ResponseEntity.ok(consultaService.cancelar(id, solicitante));
    }
}
