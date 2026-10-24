package com.example.appsaudebackend.Modules.Paciente.Service;

import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteModel;
import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteRepository;
import com.example.appsaudebackend.Shared.Exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Porta de entrada do módulo Paciente para os outros módulos.
 * Quem precisa de um paciente (ex: Consulta) chama este Service,
 * em vez de usar o PacienteRepository direto.
 */
@Service
@RequiredArgsConstructor
public class PacienteService {
    private final PacienteRepository pacienteRepository;

    public PacienteModel buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }

    public PacienteModel buscarPorUsuarioAuthId(Long usuarioAuthId) {
        return pacienteRepository.findByUsuarioAuthId(usuarioAuthId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }
}
