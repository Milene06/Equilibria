package pe.upc.equilibria.attendance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.course.Curso;
import pe.upc.equilibria.user.Usuario;
import java.time.LocalDate;

@Entity @Table(name="asistencia") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asistencia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_asistencia") private Long idAsistencia;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="id_usuario",nullable=false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="id_curso",nullable=false)
    @JsonIgnoreProperties({"usuario","hibernateLazyInitializer","handler"})
    private Curso curso;

    @Column(nullable=false) private LocalDate fecha;
    @Column(nullable=false,length=20) @Builder.Default private String estado="ASISTIO";
}