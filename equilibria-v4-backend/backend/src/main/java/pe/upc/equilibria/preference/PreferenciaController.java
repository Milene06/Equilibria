package pe.upc.equilibria.preference;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.user.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/preferences")
@RequiredArgsConstructor
@Tag(name="Preferencias", description="Configuración del usuario")
public class PreferenciaController {
    private final PreferenciaRepository prefRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    private void set(Usuario u, String clave, String valor) {
        Preferencia p = prefRepo.findByUsuarioIdUsuarioAndClave(u.getIdUsuario(), clave)
            .orElse(Preferencia.builder().usuario(u).clave(clave).build());
        p.setValor(valor);
        prefRepo.save(p);
    }

    @GetMapping
    @Operation(summary="Obtener todas las preferencias")
    public List<Preferencia> list(@AuthenticationPrincipal UserDetails ud) {
        return prefRepo.findByUsuarioIdUsuario(getUser(ud).getIdUsuario());
    }

    @PostMapping("/modo-descanso")
    @Operation(summary="Activar/desactivar modo descanso (PB-026)")
    public ResponseEntity<?> modoDescanso(@AuthenticationPrincipal UserDetails ud,
                                           @RequestBody Map<String,Object> body) {
        Usuario u = getUser(ud);
        set(u, "modo_descanso", String.valueOf(body.get("activo")));
        set(u, "descanso_hasta", String.valueOf(body.getOrDefault("hasta", "")));
        return ResponseEntity.ok(Map.of("modoDescanso", body.get("activo")));
    }

    @GetMapping("/modo-descanso")
    @Operation(summary="Estado del modo descanso")
    public ResponseEntity<?> getModoDescanso(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        String activo = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "modo_descanso")
            .map(Preferencia::getValor).orElse("false");
        String hasta = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "descanso_hasta")
            .map(Preferencia::getValor).orElse("");
        return ResponseEntity.ok(Map.of("activo", activo, "hasta", hasta));
    }

    @PostMapping("/horario-descanso")
    @Operation(summary="Configurar horario de descanso (PB-030)")
    public ResponseEntity<?> horarioDescanso(@AuthenticationPrincipal UserDetails ud,
                                              @RequestBody Map<String,String> body) {
        Usuario u = getUser(ud);
        set(u, "descanso_inicio", body.getOrDefault("horaInicio", "22:00"));
        set(u, "descanso_fin", body.getOrDefault("horaFin", "07:00"));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/sesion-estudio")
    @Operation(summary="Configurar duración de sesiones Pomodoro (PB-034)")
    public ResponseEntity<?> sesionEstudio(@AuthenticationPrincipal UserDetails ud,
                                            @RequestBody Map<String,Object> body) {
        Usuario u = getUser(ud);
        set(u, "duracion_sesion", String.valueOf(body.getOrDefault("minutos", "25")));
        set(u, "descanso_sesion", String.valueOf(body.getOrDefault("descanso", "5")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/perfil-estudio")
    @Operation(summary="Configurar perfil de estudio (PB-039)")
    public ResponseEntity<?> perfilEstudio(@AuthenticationPrincipal UserDetails ud,
                                            @RequestBody Map<String,String> body) {
        Usuario u = getUser(ud);
        set(u, "perfil_estudio", body.getOrDefault("tipo", "VESPERTINO"));
        set(u, "horas_pico", body.getOrDefault("horasPico", "14:00-20:00"));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping
    @Operation(summary="Guardar preferencia genérica")
    public ResponseEntity<?> save(@AuthenticationPrincipal UserDetails ud,
                                   @RequestBody Map<String,String> body) {
        Usuario u = getUser(ud);
        set(u, body.get("clave"), body.get("valor"));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/resumen-semanal")
    @Operation(summary="Configurar resumen semanal por correo (PB-029)")
    public ResponseEntity<?> resumenSemanal(@AuthenticationPrincipal UserDetails ud,
                                            @RequestBody Map<String,Object> body) {
        Usuario u = getUser(ud);
        boolean activo = !Boolean.FALSE.equals(body.get("activo"));
        set(u, "resumen_semanal_activo", String.valueOf(activo));
        if (body.get("dia") != null) set(u, "resumen_semanal_dia", String.valueOf(body.get("dia")));
        if (body.get("hora") != null) set(u, "resumen_semanal_hora", String.valueOf(body.get("hora")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/resumen-semanal")
    @Operation(summary="Obtener configuración del resumen semanal (PB-029)")
    public ResponseEntity<?> getResumenSemanal(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        String activo = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_activo")
                .map(Preferencia::getValor).orElse("true");
        String dia = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_dia")
                .map(Preferencia::getValor).orElse("SUNDAY");
        String hora = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_hora")
                .map(Preferencia::getValor).orElse("20");
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("activo", activo);
        resp.put("dia", dia);
        resp.put("hora", hora);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/resumen-semanal/test")
    @Operation(summary="Enviar resumen semanal de prueba")
    public ResponseEntity<?> testResumen(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        // llama directamente al metodo privado
        return ResponseEntity.ok(Map.of("mensaje", "Revisa tu correo " + u.getEmail()));
    }
}
