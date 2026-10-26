package com.example.appsaudebackend.modules.atendente.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaAtendenteRequestDto(
        @NotBlank @Size(min = 8, max = 100) String novaSenha
) {}
