package com.example.appsaudebackend.Modules.Paciente.Dto.Response;

import java.time.LocalDateTime;

public record PacienteConsultaResponseDto(
        Long id,
        LocalDateTime dataHora,
        String medico,
        String especialidade,
        String status
) {}