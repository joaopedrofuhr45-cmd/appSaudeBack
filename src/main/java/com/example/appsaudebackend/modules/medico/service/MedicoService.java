package com.example.appsaudebackend.modules.medico.service;

import com.example.appsaudebackend.modules.medico.dto.response.MedicoResponseDto;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoRepository;
import com.example.appsaudebackend.shared.exception.RecursoNaoEncontradoException;
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
        return medicoRepository.findDistinctEspecialidades();
    }

    /** Usado por outros módulos (ex: Consulta) em vez de acessar o MedicoRepository direto. */
    public MedicoModel buscarPorId(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }

    /** Igual a buscarPorId, mas trava o médico (FOR UPDATE) até o fim da transação de quem chamou. */
    public MedicoModel buscarPorIdParaAgendamento(Long id) {
        return medicoRepository.buscarPorIdComLock(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }

    public MedicoModel buscarPorUsuarioAuthId(Long usuarioAuthId) {
        return medicoRepository.findByUsuarioAuthId(usuarioAuthId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
    }
}
