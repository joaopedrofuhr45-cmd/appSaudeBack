package com.example.appsaudebackend.modules.paciente.dto.response;

import java.time.LocalDateTime;

public record PacienteConsultaResponseDto(
        Long id,
        LocalDateTime dataHora,
        String medico,
        String especialidade,
        String status
) {}