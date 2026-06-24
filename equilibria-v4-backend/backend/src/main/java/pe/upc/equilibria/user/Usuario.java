package pe.upc.equilibria.user;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="usuario") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_usuario") private Long idUsuario;
    @Column(nullable=false,length=100) private String nombre;
    @Column(nullable=false,unique=true,length=200) private String email;
    @Column(name="password_hash",nullable=false,length=255) @JsonIgnore private String passwordHash;
    @Column(nullable=false,length=20) @Builder.Default private String rol="estudiante";
    @Column(name="created_at",nullable=false,updatable=false) @Builder.Default private Instant createdAt=Instant.now();
}