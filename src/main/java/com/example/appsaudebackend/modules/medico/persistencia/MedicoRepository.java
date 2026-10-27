package com.example.appsaudebackend.modules.medico.persistencia;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<MedicoModel, Long> {
    Optional<MedicoModel> findByUsuarioAuthId(Long usuarioAuthId);
    Optional<MedicoModel> findByUsuarioAuthEmailIgnoreCase(String email);
    Optional<MedicoModel> findByEmailIgnoreCase(String email);
    Optional<MedicoModel> findByEmailAndIdNot(String email, Long id);
    List<MedicoModel> findByEspecialidadeOrderByNomeAsc(String especialidade);
    List<MedicoModel> findAllByOrderByNomeAsc();

    /** SELECT ... FOR UPDATE: precisa ser chamado dentro de uma transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MedicoModel m where m.id = :id")
    Optional<MedicoModel> buscarPorIdComLock(@Param("id") Long id);

    @Query("select distinct m.especialidade from MedicoModel m where m.especialidade is not null order by m.especialidade")
    List<String> findDistinctEspecialidades();
}
