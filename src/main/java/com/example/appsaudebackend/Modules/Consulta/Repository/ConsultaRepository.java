package com.example.appsaudebackend.Modules.Consulta.Repository;

import com.example.appsaudebackend.Modules.Consulta.Model.Consulta;
import com.example.appsaudebackend.Modules.Consulta.Model.StatusConsulta;
import com.example.appsaudebackend.Modules.Paciente.Persistencia.PacienteModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByMedicoIdAndDataHoraBetweenOrderByDataHoraAsc(Long medicoId, LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByPacienteOrderByDataHoraAsc(PacienteModel paciente);
    List<Consulta> findByPacienteAndStatusOrderByDataHoraDesc(PacienteModel paciente, StatusConsulta status);
    boolean existsByMedicoIdAndDataHora(Long medicoId, LocalDateTime dataHora);
}
