package com.example.appsaudebackend.Modules.Paciente.Dto.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AlterarSenhaRequestDto(
 @NotBlank String senhaAtual,
 @NotBlank @Size(min=8,max=100) String novaSenha
) {}