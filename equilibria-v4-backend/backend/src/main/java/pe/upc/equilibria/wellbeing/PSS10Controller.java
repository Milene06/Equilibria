package pe.upc.equilibria.wellbeing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.ai.wellness.WellnessAdvisor;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.user.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/wellbeing")
@RequiredArgsConstructor
@Tag(name="Bienestar", description="PSS-10 y bienestar emocional")
public class PSS10Controller {
    private final Pss10Repository pss10Repo;
    private final HistorialEstresRepository histRepo;
    private final UsuarioRepository userRepo;
    private final WellnessAdvisor wellnessAdvisor;
    private final TareaRepository tareaRepo;

    private static final List<String> PREGUNTAS = List.of(
        "¿Con qué frecuencia te has sentido afectado por algo que ocurrió inesperadamente?",
        "¿Con qué frecuencia te has sentido incapaz de controlar cosas importantes en tu vida?",
        "¿Con qué frecuencia te has sentido nervioso o estresado?",
        "¿Con qué frecuencia te has sentido seguro sobre tu capacidad de manejar tus problemas?",
        "¿Con qué frecuencia has sentido que las cosas van como tú quieres?",
        "¿Con qué frecuencia has sentido que no podías afrontar todo lo que tenías que hacer?",
        "¿Con qué frecuencia has podido controlar las dificultades de tu vida?",
        "¿Con qué frecuencia te has sentido al tanto y en control de las cosas?",
        "¿Con qué frecuencia te has enfadado por cosas fuera de tu control?",
        "¿Con qué frecuencia has sentido que las dificultades se acumulan tanto que no puedes superarlas?"
    );
    private static final Set<Integer> PSS10_INVERTIDOS = new HashSet<>(Arrays.asList(4, 5, 7, 8));

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/pss10/cuestionario")
    @Operation(summary="Obtener preguntas PSS-10")
    public ResponseEntity<?> cuestionario() {
        List<Map<String,Object>> qs = new ArrayList<>();
        for (int i = 0; i < PREGUNTAS.size(); i++) {
            qs.add(Map.of("numero", i+1, "pregunta", PREGUNTAS.get(i),
                "invertida", PSS10_INVERTIDOS.contains(i+1)));
        }
        return ResponseEntity.ok(Map.of("preguntas", qs,
            "opciones", List.of("Nunca","Casi nunca","De vez en cuando","A menudo","Muy a menudo")));
    }

    @PostMapping("/pss10")
    @Operation(summary = "Enviar respuestas PSS-10 y obtener consejos de WellnessAdvisor")
    public ResponseEntity<?> submit(@AuthenticationPrincipal UserDetails ud,
                                    @RequestBody Map<String, Integer> respuestas) {
        Usuario u = getUser(ud);
        int total = 0;
        for (int i = 1; i <= 10; i++) {
            int r = respuestas.getOrDefault(String.valueOf(i), 0);
            total += PSS10_INVERTIDOS.contains(Integer.valueOf(i)) ? (4 - r) : r;
            pss10Repo.save(Pss10Respuesta.builder().usuario(u).pregunta(i).respuesta(r).build());
        }
        String nivel = total <= 13 ? "BAJO" : total <= 26 ? "MODERADO" : "ALTO";
        String consejos = wellnessAdvisor.generarConsejos(u.getNombre(), total, nivel);
        HistorialEstres h = HistorialEstres.builder()
                .usuario(u).puntuacion(total).nivel(nivel).consejosIa(consejos).build();
        histRepo.save(h);

        Map<String, Object> resp = new HashMap<>();
        resp.put("score", total);
        resp.put("nivel", nivel);
        resp.put("consejos", consejos);
        resp.put("fecha", h.getFecha());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/historial")
    @Operation(summary="Historial de evaluaciones PSS-10")
    public ResponseEntity<?> historial(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        return ResponseEntity.ok(histRepo.findByUsuarioIdUsuarioOrderByFechaDesc(uid));
    }

    @GetMapping("/frase")
    @Operation(summary = "Frase motivacional de WellnessAdvisor")
    public ResponseEntity<?> frase(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        String nivel = histRepo.findFirstByUsuarioIdUsuarioOrderByFechaDesc(u.getIdUsuario())
                .map(HistorialEstres::getNivel).orElse("MODERADO");
        String frase = wellnessAdvisor.generarFraseMotivacional(u.getNombre(), nivel);

        Map<String, Object> resp = new HashMap<>();
        resp.put("frase", frase);
        resp.put("nivel", nivel);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/tecnicas")
    @Operation(summary = "Técnicas de estudio personalizadas con IA (PB-018)")
    public ResponseEntity<?> tecnicas(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        int pss = histRepo.findFirstByUsuarioIdUsuarioOrderByFechaDesc(uid)
                .map(HistorialEstres::getPuntuacion).orElse(0);
        long tareas = tareaRepo.countByUsuarioIdUsuario(uid);
        String tecnicas = wellnessAdvisor.recomendarTecnicas(pss, (int) tareas);

        Map<String, Object> resp = new HashMap<>();
        resp.put("tecnicas", tecnicas);
        resp.put("pssScore", pss);
        return ResponseEntity.ok(resp);
    }
}
