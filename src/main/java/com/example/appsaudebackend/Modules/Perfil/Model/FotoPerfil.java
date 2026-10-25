package com.example.appsaudebackend.Modules.Perfil.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "foto_perfil")
public class FotoPerfil {

    @Id
    @Column(name = "usuario_auth_id")
    private Long usuarioAuthId;

    @Column(nullable = false)
    private byte[] conteudo;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false)
    private Long versao;
}
