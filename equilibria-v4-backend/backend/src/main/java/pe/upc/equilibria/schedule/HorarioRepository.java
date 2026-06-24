package pe.upc.equilibria.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByUsuarioIdUsuarioOrderByDiaAscHoraInicioAsc(Long usuarioId);
    List<Horario> findByCursoIdCursoAndUsuarioIdUsuario(Long cursoId, Long usuarioId);
    boolean existsByIdHorarioAndUsuarioIdUsuario(Long id, Long usuarioId);
    void deleteByCursoIdCurso(Long cursoId);
}