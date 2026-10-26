package com.example.appsaudebackend.modules.auth.repository;


import com.example.appsaudebackend.modules.auth.model.UsuarioAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioAuthRepository
        extends JpaRepository<UsuarioAuth, Long> {

    Optional<UsuarioAuth> findByEmailIgnoreCase(String email);
    Optional<UsuarioAuth> findByGoogleSub(String googleSub);
}
