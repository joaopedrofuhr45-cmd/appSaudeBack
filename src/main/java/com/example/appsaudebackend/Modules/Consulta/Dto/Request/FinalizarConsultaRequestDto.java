package com.example.appsaudebackend.Modules.Consulta.Dto.Request;

import jakarta.validation.constraints.Size;

public record FinalizarConsultaRequestDto(
        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String observacao
) {}