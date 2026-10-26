package com.example.appsaudebackend.modules.perfil.controller;

import com.example.appsaudebackend.modules.perfil.model.FotoPerfil;
import com.example.appsaudebackend.modules.perfil.service.FotoPerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

/**
 * Foto de perfil das três contas. A autorização por perfil continua na SecurityConfig
 * (/pacientes/me/** = USUARIO, /medicos/** = MEDICO, /atendentes/** = ATENDENTE);
 * a foto sempre é a da conta autenticada.
 */
@RestController
@RequestMapping({"/pacientes/me/foto", "/medicos/me/foto", "/atendentes/me/foto"})
@RequiredArgsConstructor
public class FotoPerfilController {

    private final FotoPerfilService service;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> enviar(Authentication a, @RequestParam("foto") MultipartFile foto) {
        String url = service.salvar(a.getName(), foto);
        return ResponseEntity.ok(Map.of("fotoUrl", url));
    }

    @GetMapping
    public ResponseEntity<byte[]> obter(Authentication a) {
        FotoPerfil foto = service.obter(a.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipo()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(foto.getConteudo());
    }

    @DeleteMapping
    public ResponseEntity<Void> remover(Authentication a) {
        service.remover(a.getName());
        return ResponseEntity.noContent().build();
    }
}
