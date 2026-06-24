package pe.upc.equilibria.wellbeing;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import java.time.Instant;
@Entity @Table(name="historial_estres") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialEstres {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_historial") private Long idHistorial;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) private Usuario usuario;
    @Column(nullable=false) @Builder.Default private Instant fecha=Instant.now();
    @Column(nullable=false) private Integer puntuacion;
    @Column(nullable=false,length=20) private String nivel;
    @Column(name="consejos_ia",columnDefinition="text") private String consejosIa;
}
