package pe.upc.equilibria.task;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.course.Curso;
import pe.upc.equilibria.user.Usuario;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity @Table(name="tarea") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tarea {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_tarea") private Long idTarea;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) @JsonIgnore private Usuario usuario;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_curso") @JsonIgnoreProperties({"usuario", "hibernateLazyInitializer"}) private Curso curso;
    @Column(nullable=false,length=200) private String nombre;
    @Column(name="fecha_entrega",nullable=false) private LocalDate fechaEntrega;
    @Column(nullable=false) @Builder.Default private String prioridad="media";
    @Column(length=100) private String dificultad;
    @Column(name="tiempo_estimado") private Integer tiempoEstimado;
    @Column(nullable=false) @Builder.Default private Boolean completada=false;
    @Column(length=50) private String tipo;
    @Column(columnDefinition="text") private String nota;
    @Column(name="ia_score") private Integer iaScore;
    @Column(name="ia_razon",length=500) private String iaRazon;

}
