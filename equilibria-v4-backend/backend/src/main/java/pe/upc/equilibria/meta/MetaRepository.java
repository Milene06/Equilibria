package pe.upc.equilibria.meta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MetaRepository extends JpaRepository<Meta,Long> {
    List<Meta> findByUsuarioIdUsuarioOrderByFechaFinAsc(Long uid);
    boolean existsByIdMetaAndUsuarioIdUsuario(Long id, Long uid);
}
