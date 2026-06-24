package pe.upc.equilibria.wellbeing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface HistorialEstresRepository extends JpaRepository<HistorialEstres,Long> {
    List<HistorialEstres> findByUsuarioIdUsuarioOrderByFechaDesc(Long uid);
    Optional<HistorialEstres> findFirstByUsuarioIdUsuarioOrderByFechaDesc(Long uid);
    long countByUsuarioIdUsuario(Long uid);
}
