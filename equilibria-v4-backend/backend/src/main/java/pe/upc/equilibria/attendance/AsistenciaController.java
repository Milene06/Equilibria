package pe.upc.equilibria.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.user.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController @RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name="Asistencia", description="Registro de asistencia a clases (PB-019)")
public class AsistenciaController {
    private final AsistenciaRepository asistenciaRepo;
    private final CursoRepository cursoRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary="Listar registros de asistencia")
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails ud) {
        try {
            Long uid = getUser(ud).getIdUsuario();
            var registros = asistenciaRepo.findByUsuarioIdUsuarioOrderByFechaDesc(uid);
            var resumen = cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid).stream()
                    .map(c -> {
                        var ca = asistenciaRepo.findByUsuarioIdUsuarioAndCursoIdCurso(uid, c.getIdCurso());
                        long asistio = ca.stream().filter(a -> "ASISTIO".equals(a.getEstado())).count();
                        int pct = ca.isEmpty() ? 100 : (int)(asistio * 100 / ca.size());
                        Map<String, Object> m = new HashMap<>();
                        m.put("curso", c.getNombre());
                        m.put("idCurso", c.getIdCurso());
                        m.put("total", ca.size());
                        m.put("asistio", asistio);
                        m.put("porcentaje", pct);
                        m.put("alerta", pct < 75);
                        return m;
                    }).collect(Collectors.toList());
            Map<String, Object> resp = new HashMap<>();
            resp.put("registros", registros);
            resp.put("resumen", resumen);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            System.out.println("ERROR ATTENDANCE: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping
    @Operation(summary="Registrar asistencia")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @Valid @RequestBody AsistenciaRequest rq) {
        Usuario u = getUser(ud);
        var curso = cursoRepo.findById(rq.getIdCurso())
            .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        Asistencia a = Asistencia.builder().usuario(u).curso(curso)
            .fecha(rq.getFecha() != null ? rq.getFecha() : LocalDate.now())
            .estado(rq.getEstado() != null ? rq.getEstado() : "ASISTIO").build();
        return ResponseEntity.ok(asistenciaRepo.save(a));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar registro de asistencia")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!asistenciaRepo.existsByIdAsistenciaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        asistenciaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Data static class AsistenciaRequest {
        @NotNull Long idCurso;
        LocalDate fecha;
        String estado;
    }
}
