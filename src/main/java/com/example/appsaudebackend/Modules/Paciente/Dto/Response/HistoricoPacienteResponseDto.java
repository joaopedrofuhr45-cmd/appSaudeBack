package com.example.appsaudebackend.Modules.Paciente.Dto.Response;

import java.time.LocalDateTime;

public record HistoricoPacienteResponseDto(
        Long id,
        String titulo,
        String detalhe,
        String descricao,
        LocalDateTime dataHora
) {}