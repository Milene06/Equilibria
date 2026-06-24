package pe.upc.equilibria.schedule;

import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import pe.upc.equilibria.course.Curso;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name="horario") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Horario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id_horario") private Long idHorario;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_usuario", nullable=false)
    @JsonIgnore private Usuario usuario;

    @ManyToOne(fetch=FetchType.LAZY, optional=true)
    @JoinColumn(name="id_curso", nullable=true)
    @JsonIgnore private Curso curso;

    // id del curso para que el frontend lo reciba
    @Transient
    public Long getIdCurso() { return curso != null ? curso.getIdCurso() : null; }

    @Column(nullable=false, length=20) private String dia;
    @Column(name="hora_inicio", nullable=false) private java.time.LocalTime horaInicio;
    @Column(name="hora_fin", nullable=false) private java.time.LocalTime horaFin;
    @Column(length=200) private String actividad;
    @Column(length=20) private String tipo;
    @Column(name="ia_generado") @Builder.Default private Boolean iaGenerado = false;
}
