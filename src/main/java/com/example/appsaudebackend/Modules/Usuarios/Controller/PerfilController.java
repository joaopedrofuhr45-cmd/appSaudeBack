package com.example.appsaudebackend.Modules.Usuarios.Controller;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Request.*;
import com.example.appsaudebackend.Modules.Usuarios.Dto.Response.PerfilResponseDto;
import com.example.appsaudebackend.Modules.Usuarios.Service.PerfilService;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/pacientes/me") @RequiredArgsConstructor
public class PerfilController {
 private final PerfilService service;
 @GetMapping public ResponseEntity<PerfilResponseDto> obter(@RequestAttribute("cpf") String cpf){return ResponseEntity.ok(service.obter(cpf));}
 @PutMapping public ResponseEntity<PerfilResponseDto> atualizar(@RequestAttribute("cpf") String cpf,@Valid @RequestBody AtualizarPerfilRequestDto dto){return ResponseEntity.ok(service.atualizar(cpf,dto));}
 @PatchMapping("/senha") public ResponseEntity<Void> senha(@RequestAttribute("cpf") String cpf,@Valid @RequestBody AlterarSenhaRequestDto dto){service.alterarSenha(cpf,dto);return ResponseEntity.noContent().build();}
}