package com.example.appsaudebackend.modules.consulta.dto.response;

import java.time.LocalDateTime;

public record ConsultaDetalheResponseDto(
        Long id, String paciente, String medico, String especialidade,
        String tipoConsulta, LocalDateTime dataHora, String status, String observacao
) {}