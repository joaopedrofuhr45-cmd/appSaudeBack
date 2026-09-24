package com.example.appsaudebackend.Modules.Auth.Repository;


import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioAuthRepository
        extends JpaRepository<UsuarioAuth, Long> {

    Optional<UsuarioAuth> findByCpf(String cpf);
}
