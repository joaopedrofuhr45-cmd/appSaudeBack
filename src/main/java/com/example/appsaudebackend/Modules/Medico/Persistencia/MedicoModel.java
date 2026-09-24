package com.example.appsaudebackend.Modules.Medico.Persistencia;

import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String crm;

    @Column(nullable = false)
    private String especialidade;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "usuario_auth_id",
            nullable = false,
            unique = true
    )
    private UsuarioAuth usuarioAuth;
}
