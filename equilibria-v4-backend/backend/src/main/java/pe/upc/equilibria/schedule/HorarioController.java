package pe.upc.equilibria.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.ai.schedule.ScheduleGenerator;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.preference.PreferenciaRepository;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.wellbeing.HistorialEstresRepository;
import pe.upc.equilibria.user.UsuarioRepository;
import pe.upc.equilibria.user.Usuario;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name="Horario", description="Generación de horario con IA")
public class HorarioController {
    private final ScheduleGenerator scheduleGenerator;
    private final CursoRepository cursoRepo;
    private final TareaRepository tareaRepo;
    private final PreferenciaRepository prefRepo;
    private final HistorialEstresRepository histRepo;
    private final UsuarioRepository userRepo;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar horario semanal con ScheduleGenerator (Gemini)")
    public ResponseEntity<?> generar(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        var cursos = cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid);
        var tareas = tareaRepo.findByUsuarioIdUsuarioAndCompletadaFalseOrderByFechaEntregaAsc(uid);
        String perfil = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "perfil_estudio")
                .map(p -> p.getValor()).orElse("VESPERTINO");
        String horario = scheduleGenerator.generarHorario(cursos, tareas, perfil);
        return ResponseEntity.ok(Map.of("horario", horario, "perfil", perfil));
    }

    @PostMapping("/reagendar")
    @Operation(summary = "Sugerir reagendado de tareas por estrés alto (PB-038)")
    public ResponseEntity<?> reagendar(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        var ultimo = histRepo.findFirstByUsuarioIdUsuarioOrderByFechaDesc(uid);
        int pss = ultimo.map(h -> h.getPuntuacion()).orElse(0);

        if (pss < 27) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("mensaje", "Tu nivel de estrés no requiere reagendado (PSS-10=" + pss + ")");
            return ResponseEntity.ok(resp);
        }

        var tareas = tareaRepo.findByUsuarioIdUsuarioAndCompletadaFalseOrderByFechaEntregaAsc(uid);
        String sugerencias = scheduleGenerator.sugerirReagendado(tareas, pss);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sugerencias", sugerencias);
        resp.put("pssScore", pss);
        return ResponseEntity.ok(resp);
    }
}