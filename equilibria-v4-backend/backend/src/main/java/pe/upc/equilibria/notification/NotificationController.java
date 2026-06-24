package pe.upc.equilibria.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.preference.Preferencia;
import pe.upc.equilibria.preference.PreferenciaRepository;
import pe.upc.equilibria.user.UsuarioRepository;
import java.util.Map;

@RestController @RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name="Notificaciones", description="Notificaciones push (PB-015)")
public class NotificationController {
    private final PreferenciaRepository prefRepo;
    private final UsuarioRepository userRepo;

    @PostMapping("/subscribe")
    @Operation(summary="Registrar suscripción push")
    public ResponseEntity<?> subscribe(@AuthenticationPrincipal UserDetails ud,
                                       @RequestBody Map<String,Object> body) {
        var u = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        String endpoint = body.getOrDefault("endpoint","").toString();
        Preferencia p = prefRepo.findByUsuarioIdUsuarioAndClave(u.getIdUsuario(),"push_endpoint")
            .orElse(Preferencia.builder().usuario(u).clave("push_endpoint").build());
        p.setValor(endpoint);
        prefRepo.save(p);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/unsubscribe")
    @Operation(summary="Cancelar suscripción push")
    public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal UserDetails ud) {
        var u = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        prefRepo.findByUsuarioIdUsuarioAndClave(u.getIdUsuario(),"push_endpoint")
            .ifPresent(prefRepo::delete);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
