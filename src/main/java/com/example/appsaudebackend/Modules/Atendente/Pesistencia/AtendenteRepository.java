package com.example.appsaudebackend.Modules.Atendente.Pesistencia;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface AtendenteRepository extends JpaRepository<AtendenteModel,Long>{
 Optional<AtendenteModel> findByUsuarioAuthCpf(String cpf);
 Optional<AtendenteModel> findByEmail(String email);
}