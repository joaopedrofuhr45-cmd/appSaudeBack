package com.example.appsaudebackend.modules.medico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaMedicoRequestDto(
        @NotBlank @Size(min = 8, max = 100) String novaSenha
) {}
