package pe.upc.equilibria.course;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity @Table(name="curso") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Curso {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_curso") private Long idCurso;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) @JsonIgnore private Usuario usuario;
    @Column(nullable=false,length=200) private String nombre;
    @Column(length=20) private String codigo;
    @Column private Integer creditos;
    @Column(length=7) @Builder.Default private String color="#1F4FA8";
    @Column(name="fecha_examen") private LocalDate fechaExamen;
    @Column(name="examen_rendido") @Builder.Default private Boolean examenRendido=false;

}
