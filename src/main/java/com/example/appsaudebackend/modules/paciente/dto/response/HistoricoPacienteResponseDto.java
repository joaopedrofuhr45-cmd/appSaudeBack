package com.example.appsaudebackend.modules.paciente.dto.response;

import java.time.LocalDateTime;

public record HistoricoPacienteResponseDto(
        Long id,
        String titulo,
        String detalhe,
        String descricao,
        LocalDateTime dataHora
) {}