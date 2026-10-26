package com.example.appsaudebackend.modules.medico.service;

import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import com.example.appsaudebackend.modules.consulta.dto.response.ConsultaResponseDto;
import com.example.appsaudebackend.modules.consulta.service.ConsultaService;
import com.example.appsaudebackend.modules.medico.persistencia.MedicoModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/** Visões das consultas do ponto de vista do médico logado. */
@Service
@RequiredArgsConstructor
public class MedicoConsultaService {
    private final MedicoService medicoService;
    private final ConsultaService consultaService;

    public List<ConsultaResponseDto> listarAgenda(LocalDate data, UsuarioAuth solicitante) {
        MedicoModel medico = medicoService.buscarPorUsuarioAuthId(solicitante.getId());
        return consultaService.listarAgendaDoMedico(medico.getId(), data);
    }
}
