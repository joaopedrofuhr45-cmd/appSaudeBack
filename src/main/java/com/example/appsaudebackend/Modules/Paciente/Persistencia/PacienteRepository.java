package com.example.appsaudebackend.Modules.Paciente.Persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<PacienteModel, Long> {
    Optional<PacienteModel> findByUsuarioAuthId(Long usuarioAuthId);
    Optional<PacienteModel> findByUsuarioAuthEmailIgnoreCase(String email);
    Optional<PacienteModel> findByEmail(String email);
    Optional<PacienteModel> findByEmailIgnoreCase(String email);
    List<PacienteModel> findTop20ByOrderByNomeAsc();
    List<PacienteModel> findTop20ByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
