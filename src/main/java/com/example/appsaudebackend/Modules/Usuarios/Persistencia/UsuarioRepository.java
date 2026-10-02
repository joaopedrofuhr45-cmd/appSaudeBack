package com.example.appsaudebackend.Modules.Usuarios.Persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {
    Optional<UsuarioModel> findByUsuarioAuthId(Long usuarioAuthId);
    Optional<UsuarioModel> findByUsuarioAuthEmailIgnoreCase(String email);
    Optional<UsuarioModel> findByEmail(String email);
    Optional<UsuarioModel> findByEmailIgnoreCase(String email);
    List<UsuarioModel> findTop20ByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
