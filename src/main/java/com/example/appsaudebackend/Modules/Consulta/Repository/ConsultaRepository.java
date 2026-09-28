package com.example.appsaudebackend.Modules.Consulta.Repository;

import com.example.appsaudebackend.Modules.Consulta.Model.Consulta;
import com.example.appsaudebackend.Modules.Consulta.Model.StatusConsulta;
import com.example.appsaudebackend.Modules.Usuarios.Persistencia.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    List<Consulta> findByPacienteOrderByDataHoraAsc(UsuarioModel paciente);

    List<Consulta> findByPacienteAndStatusOrderByDataHoraDesc(UsuarioModel paciente, StatusConsulta status);
}