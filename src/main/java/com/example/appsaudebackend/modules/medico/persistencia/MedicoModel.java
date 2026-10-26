package com.example.appsaudebackend.modules.medico.persistencia;
import com.example.appsaudebackend.modules.auth.model.UsuarioAuth; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="medicos") @Getter @Setter @NoArgsConstructor @AllArgsConstructor public class MedicoModel{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String nome; @Column(nullable=false,unique=true) private String crm; @Column(nullable=false) private String especialidade; @Column private String email; @Column private String telefone; @OneToOne(optional=false) @JoinColumn(name="usuario_auth_id",nullable=false,unique=true) private UsuarioAuth usuarioAuth;
}