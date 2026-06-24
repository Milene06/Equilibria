package pe.upc.equilibria.course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CursoRepository extends JpaRepository<Curso,Long> {
    List<Curso> findByUsuarioIdUsuarioOrderByNombreAsc(Long idUsuario);
    boolean existsByIdCursoAndUsuarioIdUsuario(Long id, Long uid);
}
