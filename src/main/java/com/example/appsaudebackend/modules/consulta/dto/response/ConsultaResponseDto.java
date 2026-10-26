package com.example.appsaudebackend.modules.consulta.dto.response;

import java.time.LocalDateTime;

public record ConsultaResponseDto(
        Long id,
        String horario,
        String nomePaciente,
        String detalhe,
        String status,
        LocalDateTime dataHora
) {}