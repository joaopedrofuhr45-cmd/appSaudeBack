package com.example.appsaudebackend.Modules.Usuarios.Persistencia;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "usuario_auth_id",
            nullable = false,
            unique = true
    )
    private UsuarioAuth usuarioAuth;
}
