package pe.upc.equilibria.storage;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.task.Tarea;
import java.time.Instant;
@Entity @Table(name="archivo_tarea") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArchivoTarea {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_archivo") private Long idArchivo;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_tarea",nullable=false) private Tarea tarea;
    @Column(name="nombre_archivo",nullable=false,length=255) private String nombreArchivo;
    @Column(name="url_archivo",nullable=false,columnDefinition="text") private String urlArchivo;
    @Column(name="fecha_subida") @Builder.Default private Instant fechaSubida=Instant.now();
    @Column(length=50) private String tipo;
}
