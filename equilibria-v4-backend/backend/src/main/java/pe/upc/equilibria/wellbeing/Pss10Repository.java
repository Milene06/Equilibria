package pe.upc.equilibria.wellbeing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface Pss10Repository extends JpaRepository<Pss10Respuesta,Long> {
    List<Pss10Respuesta> findByUsuarioIdUsuarioOrderByFechaDesc(Long uid);
    long countByUsuarioIdUsuario(Long uid);
}
