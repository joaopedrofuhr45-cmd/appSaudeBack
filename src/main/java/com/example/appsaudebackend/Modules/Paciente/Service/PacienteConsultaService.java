package com.example.appsaudebackend.Modules.Paciente.Service;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import com.example.appsaudebackend.Modules.Consulta.Service.ConsultaService;
import com.example.appsaudebackend.Modules.Paciente.Dto.Response.HistoricoPacienteResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Dto.Response.PacienteConsultaResponseDto;
import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/** Visões das consultas do ponto de vista do paciente logado. */
@Service
@RequiredArgsConstructor
public class PacienteConsultaService {
    private final PacienteService pacienteService;
    private final ConsultaService consultaService;

    public List<PacienteConsultaResponseDto> listarConsultas(UsuarioAuth solicitante) {
        PacienteModel paciente = pacienteService.buscarPorUsuarioAuthId(solicitante.getId());
        return consultaService.listarDoPaciente(paciente).stream()
                .map(c -> new PacienteConsultaResponseDto(c.getId(), c.getDataHora(), c.getMedico().getNome(),
                        c.getMedico().getEspecialidade(), c.getStatus().name()))
                .toList();
    }

    public List<HistoricoPacienteResponseDto> listarHistorico(UsuarioAuth solicitante) {
        PacienteModel paciente = pacienteService.buscarPorUsuarioAuthId(solicitante.getId());
        return consultaService.listarConcluidasDoPaciente(paciente).stream()
                .map(c -> new HistoricoPacienteResponseDto(c.getId(), c.getTipoConsulta(),
                        c.getMedico().getNome() + " · " + c.getMedico().getEspecialidade(),
                        c.getObservacao() == null ? "" : c.getObservacao(), c.getDataHora()))
                .toList();
    }
}
