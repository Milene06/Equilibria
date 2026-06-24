package pe.upc.equilibria.preference;
import jakarta.persistence.*;
import lombok.*;
import pe.upc.equilibria.user.Usuario;
@Entity @Table(name="preferencia", uniqueConstraints=@UniqueConstraint(columnNames={"id_usuario","clave"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Preferencia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_preferencia") private Long idPreferencia;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_usuario",nullable=false) private Usuario usuario;
    @Column(nullable=false,length=100) private String clave;
    @Column(columnDefinition="text") private String valor;
}
