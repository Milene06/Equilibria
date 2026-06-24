package pe.upc.equilibria.preference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PreferenciaRepository extends JpaRepository<Preferencia,Long> {
    List<Preferencia> findByUsuarioIdUsuario(Long uid);
    Optional<Preferencia> findByUsuarioIdUsuarioAndClave(Long uid, String clave);
}
