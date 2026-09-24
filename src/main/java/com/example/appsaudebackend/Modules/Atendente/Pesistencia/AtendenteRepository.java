package com.example.appsaudebackend.Modules.Atendente.Pesistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AtendenteRepository extends JpaRepository<AtendenteModel, Long> {
    Optional<AtendenteModel> findByCpf(String cpf);
}