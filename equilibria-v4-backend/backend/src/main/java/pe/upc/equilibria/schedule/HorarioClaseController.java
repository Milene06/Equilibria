package pe.upc.equilibria.schedule;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.user.Usuario;
import pe.upc.equilibria.user.UsuarioRepository;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/horarios-clase")
@RequiredArgsConstructor
@Tag(name="HorariosClase", description="Horarios de clase por curso")
public class HorarioClaseController {
    private final HorarioRepository horarioRepo;
    private final CursoRepository cursoRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<Horario> list(@AuthenticationPrincipal UserDetails ud) {
        return horarioRepo.findByUsuarioIdUsuarioOrderByDiaAscHoraInicioAsc(getUser(ud).getIdUsuario());
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @RequestBody HorarioRequest rq) {
        Usuario u = getUser(ud);
        var curso = cursoRepo.findById(rq.getIdCurso()).orElse(null);
        if (curso == null || !curso.getUsuario().getIdUsuario().equals(u.getIdUsuario())) {
            return ResponseEntity.badRequest().build();
        }
        Horario h = Horario.builder()
                .usuario(u)
                .curso(curso)
                .dia(rq.getDia())
                .horaInicio(LocalTime.parse(rq.getHoraInicio()))
                .horaFin(LocalTime.parse(rq.getHoraFin()))
                .actividad(curso.getNombre())
                .tipo("CLASE")
                .iaGenerado(false)
                .build();
        return ResponseEntity.ok(horarioRepo.save(h));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!horarioRepo.existsByIdHorarioAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        horarioRepo.deleteById(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        return ResponseEntity.ok(resp);
    }

    @Data static class HorarioRequest {
        Long idCurso;
        String dia;
        String horaInicio;
        String horaFin;
    }
}
