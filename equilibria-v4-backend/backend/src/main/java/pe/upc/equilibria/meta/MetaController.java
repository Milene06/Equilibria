package pe.upc.equilibria.meta;

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

@RestController @RequestMapping("/metas")
@RequiredArgsConstructor
@Tag(name="Metas", description="Metas de estudio semanales (PB-016)")
public class MetaController {
    private final MetaRepository metaRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping
    @Operation(summary="Listar metas del usuario")
    public List<Meta> list(@AuthenticationPrincipal UserDetails ud) {
        return metaRepo.findByUsuarioIdUsuarioOrderByFechaFinAsc(getUser(ud).getIdUsuario());
    }

    @PostMapping
    @Operation(summary="Crear meta de estudio")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @Valid @RequestBody MetaRequest rq) {
        Meta m = Meta.builder().usuario(getUser(ud))
            .descripcion(rq.getDescripcion()).horasObjetivo(rq.getHorasObjetivo())
            .fechaInicio(rq.getFechaInicio()).fechaFin(rq.getFechaFin()).build();
        return ResponseEntity.ok(metaRepo.save(m));
    }

    @PatchMapping("/{id}/progreso")
    @Operation(summary="Actualizar progreso de meta")
    public ResponseEntity<?> progreso(@AuthenticationPrincipal UserDetails ud,
                                      @PathVariable Long id,
                                      @RequestBody Map<String,Integer> body) {
        Long uid = getUser(ud).getIdUsuario();
        if (!metaRepo.existsByIdMetaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        Meta m = metaRepo.findById(id).get();
        int p = Math.min(100, Math.max(0, body.getOrDefault("progreso", m.getProgreso())));
        m.setProgreso(p);
        if (p >= 100) m.setCompletada(true);
        return ResponseEntity.ok(metaRepo.save(m));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Eliminar meta")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        Long uid = getUser(ud).getIdUsuario();
        if (!metaRepo.existsByIdMetaAndUsuarioIdUsuario(id, uid))
            return ResponseEntity.notFound().build();
        metaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Data static class MetaRequest {
        @NotBlank String descripcion;
        Integer horasObjetivo;
        @NotNull LocalDate fechaInicio;
        @NotNull LocalDate fechaFin;
    }
}
