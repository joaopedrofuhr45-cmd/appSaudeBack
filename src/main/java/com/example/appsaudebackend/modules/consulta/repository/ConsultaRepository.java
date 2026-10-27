package com.example.appsaudebackend.modules.consulta.repository;

import com.example.appsaudebackend.modules.consulta.model.Consulta;
import com.example.appsaudebackend.modules.consulta.model.StatusConsulta;
import com.example.appsaudebackend.modules.paciente.persistencia.PacienteModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByMedicoIdAndDataHoraBetweenOrderByDataHoraAsc(Long medicoId, LocalDateTime inicio, LocalDateTime fim);
    List<Consulta> findByPacienteOrderByDataHoraAsc(PacienteModel paciente);
    List<Consulta> findByPacienteAndStatusOrderByDataHoraDesc(PacienteModel paciente, StatusConsulta status);
    boolean existsByMedicoIdAndDataHoraAndStatusNot(Long medicoId, LocalDateTime dataHora, StatusConsulta status);
}
