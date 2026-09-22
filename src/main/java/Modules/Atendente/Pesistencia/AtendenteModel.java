package Modules.Atendente.Pesistencia;

import Modules.Auth.Model.UsuarioAuth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "atendentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendenteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String setor;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "usuario_auth_id",
            nullable = false,
            unique = true
    )
    private UsuarioAuth usuarioAuth;
}
