package com.example.appsaudebackend.Modules.Usuarios.Persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    Optional<UsuarioModel> findByUsuarioAuthCpf(String cpf);
    Optional<UsuarioModel> findByEmail(String email);
    List<UsuarioModel> findTop20ByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}