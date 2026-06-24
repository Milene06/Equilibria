package pe.upc.equilibria.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.attendance.AsistenciaRepository;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.meta.MetaRepository;
import pe.upc.equilibria.pdf.PdfService;
import pe.upc.equilibria.preference.*;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.user.*;
import pe.upc.equilibria.wellbeing.*;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController @RequestMapping("/stats")
@RequiredArgsConstructor
@Tag(name="Estadísticas", description="Dashboard, métricas y exportación (PB-012, PB-023, PB-025, PB-040, PB-043)")
public class StatsController {
    private final TareaRepository tareaRepo;
    private final CursoRepository cursoRepo;
    private final MetaRepository metaRepo;
    private final HistorialEstresRepository histRepo;
    private final UsuarioRepository userRepo;
    private final PreferenciaRepository prefRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final PdfService pdfService;

    private Usuario getUser(UserDetails ud) {
        return userRepo.findByEmail(ud.getUsername()).orElseThrow();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard principal del usuario")
    public ResponseEntity<?> dashboard(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        long total = tareaRepo.countByUsuarioIdUsuario(uid);
        long completadas = tareaRepo.countByUsuarioIdUsuarioAndCompletadaTrue(uid);
        long pendientes = total - completadas;
        int tasa = total > 0 ? (int)(completadas * 100 / total) : 0;
        var cursos = cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid);
        var ultimoPss = histRepo.findFirstByUsuarioIdUsuarioOrderByFechaDesc(uid);

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalTareas", total);
        resp.put("completadas", completadas);
        resp.put("pendientes", pendientes);
        resp.put("tasaFinalizacion", tasa);
        resp.put("totalCursos", cursos.size());

        if (ultimoPss.isPresent()) {
            Map<String, Object> estres = new HashMap<>();
            estres.put("score", ultimoPss.get().getPuntuacion());
            estres.put("nivel", ultimoPss.get().getNivel());
            resp.put("ultimoEstres", estres);
        } else {
            resp.put("ultimoEstres", null);
        }

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/resumen-diario")
    public ResponseEntity<?> resumenDiario(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        LocalDate hoy = LocalDate.now();
        var hoy_ = tareaRepo.findByUsuarioIdUsuarioAndFechaEntrega(uid, hoy);
        var semana = tareaRepo.findByUsuarioIdUsuarioAndFechaEntregaBetween(uid, hoy, hoy.plusDays(7));
        Map<String, Object> resp = new HashMap<>();
        resp.put("fecha", hoy);
        resp.put("vencenHoy", hoy_);
        resp.put("vencenSemana", semana);
        resp.put("totalHoy", hoy_.size());
        resp.put("totalSemana", semana.size());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/sync")
    public ResponseEntity<?> sync(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        Map<String, Object> resp = new HashMap<>();
        resp.put("tareas", tareaRepo.findByUsuarioIdUsuarioOrderByFechaEntregaAsc(uid));
        resp.put("cursos", cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid));
        resp.put("metas", metaRepo.findByUsuarioIdUsuarioOrderByFechaFinAsc(uid));
        resp.put("preferencias", prefRepo.findByUsuarioIdUsuario(uid));
        resp.put("ultimoSync", Instant.now());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/progreso-historico")
    public ResponseEntity<?> progresoHistorico(@AuthenticationPrincipal UserDetails ud) {
        Long uid = getUser(ud).getIdUsuario();
        var todas = tareaRepo.findByUsuarioIdUsuarioOrderByFechaEntregaAsc(uid);
        Map<String, Long> porSemana = todas.stream()
                .filter(t -> t.getCompletada())
                .collect(Collectors.groupingBy(t -> {
                    int year = t.getFechaEntrega().getYear();
                    int week = t.getFechaEntrega().get(WeekFields.ISO.weekOfYear());
                    return year + "-W" + String.format("%02d", week);
                }, Collectors.counting()));
        Map<String, Object> resp = new HashMap<>();
        resp.put("progresoSemanal", porSemana);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/exportar-pdf")
    @Operation(summary="Exportar plan de estudio a PDF (PB-023)")
    public ResponseEntity<byte[]> exportarPdf(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        Long uid = u.getIdUsuario();
        var tareas = tareaRepo.findByUsuarioIdUsuarioOrderByFechaEntregaAsc(uid);
        var cursos = cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid);
        byte[] pdf = pdfService.generarPlanEstudio(u.getNombre(), tareas, cursos);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plan-equilibria.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @PostMapping("/compartir-progreso")
    public ResponseEntity<?> compartirProgreso(@AuthenticationPrincipal UserDetails ud) {
        Usuario u = getUser(ud);
        String token = UUID.randomUUID().toString();
        String expira = LocalDateTime.now().plusHours(24).toString();
        Preferencia pt = prefRepo.findByUsuarioIdUsuarioAndClave(u.getIdUsuario(), "share_token")
                .orElse(Preferencia.builder().usuario(u).clave("share_token").build());
        pt.setValor(token); prefRepo.save(pt);
        Preferencia pe = prefRepo.findByUsuarioIdUsuarioAndClave(u.getIdUsuario(), "share_expira")
                .orElse(Preferencia.builder().usuario(u).clave("share_expira").build());
        pe.setValor(expira); prefRepo.save(pe);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("expira", expira);
        resp.put("url", "/stats/progreso-publico/" + token);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/progreso-publico/{token}")
    public ResponseEntity<?> progresoPublico(@PathVariable String token) {
        var ptOpt = prefRepo.findAll().stream()
                .filter(p -> "share_token".equals(p.getClave()) && token.equals(p.getValor()))
                .findFirst();
        if (ptOpt.isEmpty()) return ResponseEntity.notFound().build();
        Long uid = ptOpt.get().getUsuario().getIdUsuario();
        var expiraOpt = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "share_expira");
        if (expiraOpt.isPresent()) {
            LocalDateTime exp = LocalDateTime.parse(expiraOpt.get().getValor());
            if (LocalDateTime.now().isAfter(exp)) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "Token expirado");
                return ResponseEntity.badRequest().body(err);
            }
        }
        long total = tareaRepo.countByUsuarioIdUsuario(uid);
        long completadas = tareaRepo.countByUsuarioIdUsuarioAndCompletadaTrue(uid);
        var ultimoPss = histRepo.findFirstByUsuarioIdUsuarioOrderByFechaDesc(uid);
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalTareas", total);
        resp.put("completadas", completadas);
        resp.put("pendientes", total - completadas);
        resp.put("cursos", cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(uid).size());
        if (ultimoPss.isPresent()) {
            Map<String, Object> estres = new HashMap<>();
            estres.put("score", ultimoPss.get().getPuntuacion());
            estres.put("nivel", ultimoPss.get().getNivel());
            resp.put("ultimoEstres", estres);
        } else {
            resp.put("ultimoEstres", null);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/admin/metricas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Métricas de uso para administradores (PB-043)")
    public ResponseEntity<?> metricasAdmin() {
        return ResponseEntity.ok(Map.of(
            "totalUsuarios", userRepo.count(),
            "totalTareas", tareaRepo.count(),
            "totalCursos", cursoRepo.count(),
            "totalPss10", histRepo.count(),
            "totalMetas", metaRepo.count()
        ));
    }
}
