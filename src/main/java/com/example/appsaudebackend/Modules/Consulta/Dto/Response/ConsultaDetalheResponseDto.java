package com.example.appsaudebackend.Modules.Consulta.Dto.Response;

import java.time.LocalDateTime;

public record ConsultaDetalheResponseDto(
        Long id, String paciente, String medico, String especialidade,
        String tipoConsulta, LocalDateTime dataHora, String status, String observacao
) {}