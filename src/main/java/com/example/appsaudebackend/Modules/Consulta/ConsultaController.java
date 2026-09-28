package com.example.appsaudebackend.Modules.Consulta;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Dto.Request.ConsultaRequestDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.ConsultaResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.HistoricoPacienteResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.PacienteConsultaResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @GetMapping
    public List<ConsultaResponseDto> listarPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return consultaService.listarPorData(data);
    }

    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody ConsultaRequestDto dto,
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        consultaService.criar(dto, solicitante);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/paciente")
    public List<PacienteConsultaResponseDto> minhasConsultas(
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return consultaService.listarDoPaciente(solicitante);
    }

    @GetMapping("/paciente/historico")
    public List<HistoricoPacienteResponseDto> historico(
            @AuthenticationPrincipal UsuarioAuth solicitante) {
        return consultaService.listarHistorico(solicitante);
    }
}