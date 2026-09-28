package com.example.appsaudebackend.Modules.Usuarios.Controller;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Request.*;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Response.PerfilResponseDto;
import com.example.appsaudebackend.Modules.Usuarios.Service.PerfilService;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/pacientes/me") @RequiredArgsConstructor
public class PerfilController {
 private final PerfilService service;
 @GetMapping public ResponseEntity<PerfilResponseDto> obter(Authentication a){return ResponseEntity.ok(service.obter(a.getName()));}
 @PutMapping public ResponseEntity<PerfilResponseDto> atualizar(Authentication a,@Valid @RequestBody AtualizarPerfilRequestDto dto){return ResponseEntity.ok(service.atualizar(a.getName(),dto));}
 @PatchMapping("/senha") public ResponseEntity<Void> senha(Authentication a,@Valid @RequestBody AlterarSenhaRequestDto dto){service.alterarSenha(a.getName(),dto);return ResponseEntity.noContent().build();}
}