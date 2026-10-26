package com.example.appsaudebackend.modules.perfil.repository;

import com.example.appsaudebackend.modules.perfil.model.FotoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FotoPerfilRepository extends JpaRepository<FotoPerfil, Long> {

    /** Só a versão (sem carregar os bytes), usada para montar o fotoUrl do perfil. */
    @Query("select f.versao from FotoPerfil f where f.usuarioAuthId = :id")
    Optional<Long> buscarVersao(@Param("id") Long id);
}
