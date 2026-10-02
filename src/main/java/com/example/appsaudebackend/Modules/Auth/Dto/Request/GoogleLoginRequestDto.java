package com.example.appsaudebackend.Modules.Auth.Dto.Request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(@NotBlank String credential) {}
