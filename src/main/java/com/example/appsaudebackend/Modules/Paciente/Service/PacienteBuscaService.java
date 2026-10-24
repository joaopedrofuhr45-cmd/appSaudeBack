package com.example.appsaudebackend.Modules.Paciente.Service;

import com.example.appsaudebackend.Modules.Paciente.Dto.Response.PacienteOpcaoResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteModel;
import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteBuscaService {
    private final PacienteRepository repository;

    public List<PacienteOpcaoResponseDto> buscar(String nome) {
        String termo = nome == null ? "" : nome.trim();
        List<PacienteModel> pacientes = termo.isBlank()
                ? repository.findTop20ByOrderByNomeAsc()
                : repository.findTop20ByNomeContainingIgnoreCaseOrderByNomeAsc(termo);
        return pacientes.stream()
                .map(p -> new PacienteOpcaoResponseDto(p.getId(), p.getNome()))
                .toList();
    }
}
