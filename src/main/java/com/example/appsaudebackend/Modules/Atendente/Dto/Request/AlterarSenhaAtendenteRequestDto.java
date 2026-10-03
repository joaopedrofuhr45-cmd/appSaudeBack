package com.example.appsaudebackend.Modules.Atendente.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaAtendenteRequestDto(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 8, max = 100) String novaSenha
) {}
