package com.example.appsaudebackend.modules.paciente.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AlterarSenhaRequestDto(
 @NotBlank @Size(min=8,max=100) String novaSenha
) {}