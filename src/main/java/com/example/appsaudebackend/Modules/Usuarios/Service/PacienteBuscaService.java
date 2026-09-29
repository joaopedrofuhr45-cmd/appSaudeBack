package com.example.appsaudebackend.Modules.Usuarios.Service;

import com.example.appsaudebackend.Modules.Usuarios.Dto.Response.PacienteOpcaoResponseDto;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteBuscaService {
    private final UsuarioRepository repository;

    public List<PacienteOpcaoResponseDto> buscar(String nome) {
        String termo = nome == null ? "" : nome.trim();
        if (termo.isBlank()) {
            return repository.findAll().stream()
                    .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                    .limit(20)
                    .map(p -> new PacienteOpcaoResponseDto(p.getId(), p.getNome()))
                    .toList();
        }
        return repository.findTop20ByNomeContainingIgnoreCaseOrderByNomeAsc(termo)
                .stream()
                .map(p -> new PacienteOpcaoResponseDto(p.getId(), p.getNome()))
                .toList();
    }
}