package pe.upc.equilibria.wellbeing;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import java.time.Instant;
@Entity @Table(name="pss10_respuesta") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pss10Respuesta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_respuesta") private Long idRespuesta;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) private Usuario usuario;
    @Column(nullable=false) @Builder.Default private Instant fecha=Instant.now();
    @Column(nullable=false) private Integer pregunta;
    @Column(nullable=false) private Integer respuesta;
}
