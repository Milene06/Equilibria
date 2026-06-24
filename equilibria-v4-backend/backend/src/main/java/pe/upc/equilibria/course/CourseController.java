package pe.upc.equilibria.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.user.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name="Cursos", description="Gestión de cursos académicos")
public class CourseController {
    private final CursoRepository cursoRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary="Listar cursos del usuario")
    public List<Curso> list(@AuthenticationPrincipal UserDetails ud) {
        return cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(getUser(ud).getIdUsuario());
    }

    @PostMapping
    @Operation(summary="Crear curso")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @Valid @RequestBody CursoRequest rq) {
        Curso c = Curso.builder().usuario(getUser(ud)).nombre(rq.getNombre())
            .codigo(rq.getCodigo()).creditos(rq.getCreditos())
            .color(rq.getColor() != null ? rq.getColor() : "#1F4FA8")
            .fechaExamen(rq.getFechaExamen()).build();
        return ResponseEntity.ok(cursoRepo.save(c));
    }

    @PutMapping("/{id}")
    @Operation(summary="Actualizar curso")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails ud,
                                    @PathVariable Long id,
                                    @Valid @RequestBody CursoRequest rq) {
        Long uid = getUser(ud).getIdUsuario();
        if (!cursoRepo.existsByIdCursoAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        Curso c = cursoRepo.findById(id).get();
        c.setNombre(rq.getNombre()); c.setCodigo(rq.getCodigo());
        c.setCreditos(rq.getCreditos()); c.setColor(rq.getColor());
        c.setFechaExamen(rq.getFechaExamen());
        return ResponseEntity.ok(cursoRepo.save(c));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar curso")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!cursoRepo.existsByIdCursoAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        cursoRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Data static class CursoRequest {
        @NotBlank String nombre;
        String codigo; Integer creditos; String color; LocalDate fechaExamen;
    }
}
