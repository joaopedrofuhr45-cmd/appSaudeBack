package com.example.appsaudebackend.Modules.Consulta.Dto.Response;

import java.time.LocalDateTime;

public record ConsultaResponseDto(
        Long id,
        String horario,
        String nomePaciente,
        String detalhe,
        String status,
        LocalDateTime dataHora
) {}