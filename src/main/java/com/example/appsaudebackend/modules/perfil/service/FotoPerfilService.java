package com.example.appsaudebackend.modules.perfil.service;

import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.auth.service.AuthAccountService;
import com.example.appsaudebackend.modules.perfil.model.FotoPerfil;
import com.example.appsaudebackend.modules.perfil.repository.FotoPerfilRepository;
import com.example.appsaudebackend.shared.exception.RecursoNaoEncontradoException;
import com.example.appsaudebackend.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FotoPerfilService {

    public static final long TAMANHO_MAXIMO = 2L * 1024 * 1024;

    private final FotoPerfilRepository repository;
    private final AuthAccountService accountService;

    /** Caminho relativo (a raiz da API é resolvida pelo front), ex.: /pacientes/me/foto?v=123 */
    public String fotoUrl(UsuarioAuth auth) {
        if (auth == null || auth.getId() == null) return null;
        return repository.buscarVersao(auth.getId())
                .map(v -> prefixo(auth) + "/me/foto?v=" + v)
                .orElse(null);
    }

    @Transactional
    public String salvar(String email, MultipartFile arquivo) {
        UsuarioAuth auth = encontrar(email);
        if (arquivo == null || arquivo.isEmpty()) throw new RegraNegocioException("Envie uma imagem.");
        if (arquivo.getSize() > TAMANHO_MAXIMO) throw new RegraNegocioException("A imagem deve ter no máximo 2 MB.");

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler a imagem enviada.");
        }
        // Não confiamos no Content-Type do cliente: o tipo vem dos primeiros bytes do arquivo.
        String tipo = detectarTipo(bytes);
        if (tipo == null) throw new RegraNegocioException("Use uma imagem JPG, PNG ou WebP.");

        FotoPerfil foto = repository.findById(auth.getId()).orElseGet(() -> {
            FotoPerfil nova = new FotoPerfil();
            nova.setUsuarioAuthId(auth.getId());
            return nova;
        });
        foto.setConteudo(bytes);
        foto.setTipo(tipo);
        foto.setVersao(System.currentTimeMillis());
        repository.save(foto);
        return fotoUrl(auth);
    }

    @Transactional(readOnly = true)
    public FotoPerfil obter(String email) {
        UsuarioAuth auth = encontrar(email);
        return repository.findById(auth.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Foto não encontrada."));
    }

    @Transactional
    public void remover(String email) {
        UsuarioAuth auth = encontrar(email);
        repository.findById(auth.getId()).ifPresent(repository::delete);
    }

    private UsuarioAuth encontrar(String email) {
        return accountService.encontrarPorEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }

    private String prefixo(UsuarioAuth auth) {
        return switch (auth.getRole()) {
            case USUARIO -> "/pacientes";
            case MEDICO -> "/medicos";
            case ATENDENTE -> "/atendentes";
        };
    }

    static String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) return "image/png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        return null;
    }
}
