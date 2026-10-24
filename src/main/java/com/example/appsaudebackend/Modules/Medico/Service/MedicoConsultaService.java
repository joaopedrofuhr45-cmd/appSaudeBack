package com.example.appsaudebackend.Modules.Medico.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Dto.Response.ConsultaResponseDto;
import com.example.appsaudebackend.Modules.Consulta.Service.ConsultaService;
import com.example.appsaudebackend.Modules.Medico.Persistencia.MedicoModel;
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
