package pe.upc.equilibria.task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface TareaRepository extends JpaRepository<Tarea,Long> {
    List<Tarea> findByUsuarioIdUsuarioOrderByFechaEntregaAsc(Long uid);
    List<Tarea> findByUsuarioIdUsuarioAndCompletadaFalseOrderByFechaEntregaAsc(Long uid);
    List<Tarea> findByUsuarioIdUsuarioAndFechaEntregaBetween(Long uid, LocalDate from, LocalDate to);
    List<Tarea> findByUsuarioIdUsuarioAndFechaEntrega(Long uid, LocalDate fecha);
    boolean existsByIdTareaAndUsuarioIdUsuario(Long id, Long uid);
    long countByUsuarioIdUsuario(Long uid);
    long countByUsuarioIdUsuarioAndCompletadaTrue(Long uid);
}
