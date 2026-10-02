package com.example.appsaudebackend.Modules.Medico.Persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<MedicoModel, Long> {
    Optional<MedicoModel> findByUsuarioAuthId(Long usuarioAuthId);
    Optional<MedicoModel> findByUsuarioAuthEmailIgnoreCase(String email);
    Optional<MedicoModel> findByEmailIgnoreCase(String email);
    Optional<MedicoModel> findByEmailAndIdNot(String email, Long id);
    List<MedicoModel> findByEspecialidadeOrderByNomeAsc(String especialidade);
    List<MedicoModel> findAllByOrderByNomeAsc();
}
