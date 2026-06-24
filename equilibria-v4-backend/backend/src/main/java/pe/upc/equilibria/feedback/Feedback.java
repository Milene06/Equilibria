package pe.upc.equilibria.feedback;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import java.time.Instant;
@Entity @Table(name="feedback") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Feedback {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_feedback") private Long idFeedback;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) private Usuario usuario;
    @Column(name="id_recomendacion") private Long idRecomendacion;
    @Column(nullable=false) private Boolean util;
    @Column(columnDefinition="text") private String comentario;
    @Column(nullable=false) @Builder.Default private Instant fecha=Instant.now();
    @Column(length=30) private String tipo;
}
