package pe.upc.equilibria.storage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ArchivoTareaRepository extends JpaRepository<ArchivoTarea,Long> {
    List<ArchivoTarea> findByTareaIdTarea(Long tareaId);
}
