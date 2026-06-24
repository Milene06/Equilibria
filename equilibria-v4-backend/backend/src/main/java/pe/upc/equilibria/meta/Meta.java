package pe.upc.equilibria.meta;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
import java.time.LocalDate;
@Entity @Table(name="meta") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_meta") private Long idMeta;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) private Usuario usuario;
    @Column(nullable=false,length=300) private String descripcion;
    @Column(name="horas_objetivo") private Integer horasObjetivo;
    @Column(name="fecha_inicio",nullable=false) private LocalDate fechaInicio;
    @Column(name="fecha_fin",nullable=false) private LocalDate fechaFin;
    @Column @Builder.Default private Integer progreso=0;
    @Column(nullable=false) @Builder.Default private Boolean completada=false;
}
