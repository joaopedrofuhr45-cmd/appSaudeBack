package com.example.appsaudebackend.Modules.Medico.Service;

import com.example.appsaudebackend.Modules.Medico.Dto.Response.MedicoResponseDto;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {
    private final MedicoRepository medicoRepository;

    public List<MedicoResponseDto> listar(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            return medicoRepository.findAllByOrderByNomeAsc().stream()
                    .map(m -> new MedicoResponseDto(m.getId(), m.getNome()))
                    .toList();
        }
        return medicoRepository.findByEspecialidadeOrderByNomeAsc(especialidade).stream()
                .map(m -> new MedicoResponseDto(m.getId(), m.getNome()))
                .toList();
    }

    public List<String> listarEspecialidades() {
        return medicoRepository.findAllByOrderByNomeAsc().stream()
                .map(m -> m.getEspecialidade())
                .distinct()
                .sorted()
                .toList();
    }
}
