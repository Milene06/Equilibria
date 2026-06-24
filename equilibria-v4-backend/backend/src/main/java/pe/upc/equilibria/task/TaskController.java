package pe.upc.equilibria.task;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.ai.priority.PriorityAlgorithm;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.user.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name="Tareas", description="Gestión de tareas académicas")
public class TaskController {
    private final TareaRepository tareaRepo;
    private final UsuarioRepository userRepo;
    private final CursoRepository cursoRepo;
    private final PriorityAlgorithm priorityAlgorithm;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary = "Listar tareas del usuario")
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails ud,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) String tipo) {
        Long uid = getUser(ud).getIdUsuario();
        List<Tarea> all = tareaRepo.findByUsuarioIdUsuarioOrderByFechaEntregaAsc(uid);

        if (estado != null && !estado.isEmpty()) {
            if ("pendientes".equals(estado))
                all = all.stream().filter(t -> !t.getCompletada()).toList();
            else if ("completadas".equals(estado))
                all = all.stream().filter(Tarea::getCompletada).toList();
        }

        if (tipo != null && !tipo.isEmpty()) {
            all = all.stream().filter(t -> tipo.equals(t.getTipo())).toList();
        }
        return ResponseEntity.ok(all);
    }

    @PostMapping
    @Operation(summary="Crear tarea")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @Valid @RequestBody TareaRequest rq) {
        Usuario u = getUser(ud);
        Tarea t = Tarea.builder().usuario(u).nombre(rq.getNombre())
            .fechaEntrega(rq.getFechaEntrega()).prioridad(rq.getPrioridad() != null ? rq.getPrioridad() : "media")
            .dificultad(rq.getDificultad()).tiempoEstimado(rq.getTiempoEstimado())
            .tipo(rq.getTipo()).nota(rq.getNota()).build();
        if (rq.getIdCurso() != null)
            cursoRepo.findById(rq.getIdCurso()).ifPresent(t::setCurso);
        return ResponseEntity.ok(tareaRepo.save(t));
    }

    @PutMapping("/{id}")
    @Operation(summary="Actualizar tarea")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails ud,
                                    @PathVariable Long id,
                                    @Valid @RequestBody TareaRequest rq) {
        Long uid = getUser(ud).getIdUsuario();
        if (!tareaRepo.existsByIdTareaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        Tarea t = tareaRepo.findById(id).get();
        t.setNombre(rq.getNombre()); t.setFechaEntrega(rq.getFechaEntrega());
        if (rq.getPrioridad() != null) t.setPrioridad(rq.getPrioridad());
        t.setDificultad(rq.getDificultad()); t.setTiempoEstimado(rq.getTiempoEstimado());
        t.setTipo(rq.getTipo()); t.setNota(rq.getNota());
        if (rq.getCompletada() != null) t.setCompletada(rq.getCompletada());
        if (rq.getIdCurso() != null)
            cursoRepo.findById(rq.getIdCurso()).ifPresent(t::setCurso);
        return ResponseEntity.ok(tareaRepo.save(t));
    }

    @PatchMapping("/{id}/completar")
    @Operation(summary="Marcar tarea como completada/pendiente")
    public ResponseEntity<?> toggleCompletar(@AuthenticationPrincipal UserDetails ud,
                                              @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!tareaRepo.existsByIdTareaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        Tarea t = tareaRepo.findById(id).get();
        t.setCompletada(!t.getCompletada());
        return ResponseEntity.ok(tareaRepo.save(t));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar tarea")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!tareaRepo.existsByIdTareaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        tareaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/priorizar")
    @Operation(summary="Priorizar tareas con IA (PriorityAlgorithm)")
    public ResponseEntity<?> priorizar(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        List<Tarea> pendientes = tareaRepo.findByUsuarioIdUsuarioAndCompletadaFalseOrderByFechaEntregaAsc(uid);
        if (pendientes.isEmpty()) return ResponseEntity.ok(Map.of("mensaje", "No hay tareas pendientes"));
        List<Tarea> priorizadas = priorityAlgorithm.priorizar(pendientes);
        tareaRepo.saveAll(priorizadas);
        return ResponseEntity.ok(priorizadas);
    }

    @Data static class TareaRequest {
        @NotBlank String nombre;
        @NotNull LocalDate fechaEntrega;
        String prioridad; String dificultad; Integer tiempoEstimado;
        String tipo; String nota; Boolean completada; Long idCurso;
    }
}
