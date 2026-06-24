package pe.upc.equilibria.attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AsistenciaRepository extends JpaRepository<Asistencia,Long> {
    List<Asistencia> findByUsuarioIdUsuarioOrderByFechaDesc(Long uid);
    List<Asistencia> findByUsuarioIdUsuarioAndCursoIdCurso(Long uid, Long cursoId);
    boolean existsByIdAsistenciaAndUsuarioIdUsuario(Long id, Long uid);
}
