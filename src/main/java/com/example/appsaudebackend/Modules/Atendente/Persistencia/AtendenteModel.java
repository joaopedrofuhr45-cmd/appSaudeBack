package com.example.appsaudebackend.Modules.Atendente.Persistencia;
import com.example.appsaudebackend.Modules.Auth.Model.UsuarioAuth; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="atendentes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor public class AtendenteModel{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String nome; @Column private String email; @Column private String telefone; @Column(nullable=false) private String setor; @OneToOne(optional=false) @JoinColumn(name="usuario_auth_id",nullable=false,unique=true) private UsuarioAuth usuarioAuth;
}