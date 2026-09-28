package com.example.appsaudebackend.Modules.Usuarios.Dto.Request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AtualizarPerfilRequestDto(
 @NotBlank @Size(max=150) String nome,
 @NotBlank @Email @Size(max=150) String email,
 @NotBlank @Size(max=30) String telefone
) {}