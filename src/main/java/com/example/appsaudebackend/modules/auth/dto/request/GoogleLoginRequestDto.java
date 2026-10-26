package com.example.appsaudebackend.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(@NotBlank String credential) {}
