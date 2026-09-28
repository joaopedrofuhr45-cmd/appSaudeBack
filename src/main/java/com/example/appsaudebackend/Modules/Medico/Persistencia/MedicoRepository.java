package com.example.appsaudebackend.Modules.Medico.Persistencia;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface MedicoRepository extends JpaRepository<MedicoModel,Long>{
 Optional<MedicoModel> findByUsuarioAuthCpf(String cpf);
 Optional<MedicoModel> findByEmail(String email);
 List<MedicoModel> findByEspecialidadeOrderByNomeAsc(String especialidade);
 List<MedicoModel> findAllByOrderByNomeAsc();
}