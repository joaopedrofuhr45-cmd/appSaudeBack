package com.example.appsaudebackend.Modules.Conta.Controller;

import com.example.appsaudebackend.Modules.Conta.Dto.Request.AlterarSenhaRequestDto;
import com.example.appsaudebackend.Modules.Conta.Service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conta/me")
@RequiredArgsConstructor
public class ContaController {
    private final ContaService service;

    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(Authentication a, @Valid @RequestBody AlterarSenhaRequestDto dto) {
        service.alterarSenha(a.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}