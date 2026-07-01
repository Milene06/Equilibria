package pe.upc.equilibria.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.equilibria.ai.schedule.ScheduleGenerator;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.preference.PreferenciaRepository;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.wellbeing.HistorialEstresRepository;
import pe.upc.equilibria.user.UsuarioRepository;
import pe.upc.equilibria.user.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
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
    private final HorarioRepository horarioRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    private static final List<String> DIAS_VALIDOS = List.of("LUNES","MARTES","MIÉRCOLES","JUEVES","VIERNES","SÁBADO");

    private String normalizarDia(String dia) {
        if (dia == null) return "LUNES";
        String d = dia.trim().toUpperCase();
        
        if (d.equals("MIERCOLES")) return "MIÉRCOLES";
        return DIAS_VALIDOS.contains(d) ? d : "LUNES";
    }

    @PostMapping("/generar")
    @Transactional
    @Operation(summary = "Generar horario semanal con ScheduleGenerator (IA) y guardarlo")
    public ResponseEntity<?> generar(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        var cursos = cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid);
        var tareas = tareaRepo.findByUsuarioIdUsuarioAndCompletadaFalseOrderByFechaEntregaAsc(uid);
        String perfil = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "perfil_estudio")
                .map(p -> p.getValor()).orElse("VESPERTINO");
        String horarioJson = scheduleGenerator.generarHorario(cursos, tareas, perfil);

        // Borrar el horario IA anterior de este usuario antes de guardar el nuevo
        horarioRepo.deleteByUsuarioIdUsuarioAndIaGeneradoTrue(uid);

        try {
            List<Map<String,Object>> bloques = objectMapper.readValue(horarioJson, List.class);
            for (Map<String,Object> b : bloques) {
                try {
                    Horario h = Horario.builder()
                            .usuario(u)
                            .curso(null)
                            .dia(normalizarDia((String) b.get("dia")))
                            .horaInicio(LocalTime.parse((String) b.get("horaInicio")))
                            .horaFin(LocalTime.parse((String) b.get("horaFin")))
                            .actividad((String) b.get("actividad"))
                            .tipo((String) b.getOrDefault("tipo", "ESTUDIO"))
                            .iaGenerado(true)
                            .build();
                    horarioRepo.save(h);
                } catch (Exception ignore) { /* salta bloques mal formados */ }
            }
        } catch (Exception e) {
            System.out.println("No se pudo parsear/guardar horario IA: " + e.getMessage());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("horario", horarioJson);
        resp.put("perfil", perfil);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reagendar")
    @Operation(summary = "Sugerir reagendado de tareas por estrés alto")
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
