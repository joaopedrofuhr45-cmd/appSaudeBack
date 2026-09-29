package com.example.appsaudebackend.Modules.Medico.Persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<MedicoModel, Long> {
    Optional<MedicoModel> findByUsuarioAuthCpf(String cpf);
    List<MedicoModel> findByEspecialidadeOrderByNomeAsc(String especialidade);
    List<MedicoModel> findAllByOrderByNomeAsc();
}