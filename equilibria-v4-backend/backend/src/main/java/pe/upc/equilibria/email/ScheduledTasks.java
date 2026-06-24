package pe.upc.equilibria.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.upc.equilibria.meta.Meta;
import pe.upc.equilibria.meta.MetaRepository;
import pe.upc.equilibria.preference.Preferencia;
import pe.upc.equilibria.preference.PreferenciaRepository;
import pe.upc.equilibria.task.Tarea;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.user.Usuario;
import pe.upc.equilibria.user.UsuarioRepository;
import pe.upc.equilibria.wellbeing.HistorialEstres;
import pe.upc.equilibria.wellbeing.HistorialEstresRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component @RequiredArgsConstructor @Slf4j
public class ScheduledTasks {
    private final EmailService emailService;
    private final UsuarioRepository userRepo;
    private final TareaRepository tareaRepo;
    private final MetaRepository metaRepo;
    private final HistorialEstresRepository histRepo;
    private final PreferenciaRepository prefRepo;

    private static final DayOfWeek DEFAULT_DIA = DayOfWeek.SUNDAY;
    private static final int DEFAULT_HORA = 20;

    @Scheduled(cron = "0 * * * * *")
    public void evaluarEnvioResumenes() {
        LocalDateTime ahora = LocalDateTime.now();
        DayOfWeek diaActual = ahora.getDayOfWeek();
        int horaActual = ahora.getHour();

        List<Usuario> usuarios = userRepo.findAll();
        for (Usuario u : usuarios) {
            try {
                if (!correspondeEnviarAhora(u, diaActual, horaActual)) continue;
                enviarResumenA(u);
            } catch (Exception e) {
                log.error("Error evaluando resumen semanal para usuario {}: {}", u.getIdUsuario(), e.getMessage());
            }
        }
    }

    private boolean correspondeEnviarAhora(Usuario u, DayOfWeek diaActual, int horaActual) {
        Long uid = u.getIdUsuario();

        boolean activo = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_activo")
        .map(Preferencia::getValor)
        .map(v -> !"false".equalsIgnoreCase(v))
        .orElse(true);
        if (!activo) return false;

        DayOfWeek diaConfigurado = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_dia")
        .map(Preferencia::getValor)
        .map(this::parseDia)
        .orElse(DEFAULT_DIA);
        int horaConfigurada = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "resumen_semanal_hora")
        .map(Preferencia::getValor)
        .map(this::parseHora)
        .orElse(DEFAULT_HORA);

        return diaActual == diaConfigurado && horaActual == horaConfigurada;
    }

    private DayOfWeek parseDia(String valor) {
        try { return DayOfWeek.valueOf(valor.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { return DEFAULT_DIA; }
    }

    private int parseHora(String valor) {
        try {
            String limpio = valor.trim();
            if (limpio.contains(":")) limpio = limpio.split(":")[0];
            int h = Integer.parseInt(limpio);
            return (h >= 0 && h <= 23) ? h : DEFAULT_HORA;
        } catch (Exception e) { return DEFAULT_HORA; }
    }

    private void enviarResumenA(Usuario u) {
        Long uid = u.getIdUsuario();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(6);

        List<Tarea> tareasSemana = tareaRepo.findByUsuarioIdUsuarioAndFechaEntregaBetween(uid, inicioSemana, hoy);
        long completadas = tareasSemana.stream().filter(Tarea::getCompletada).count();
        long pendientes = tareasSemana.size() - completadas;

        List<Meta> metas = metaRepo.findByUsuarioIdUsuarioOrderByFechaFinAsc(uid);
        double porcentajeMetas = metas.isEmpty() ? 0 :
                metas.stream().mapToInt(m -> m.getProgreso() == null ? 0 : m.getProgreso()).average().orElse(0);

        List<HistorialEstres> historial = histRepo.findByUsuarioIdUsuarioOrderByFechaDesc(uid);
        Instant inicioInstant = inicioSemana.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        List<HistorialEstres> historialSemana = historial.stream()
                .filter(h -> h.getFecha() != null && !h.getFecha().isBefore(inicioInstant))
                .toList();
        Double estresPromedio = historialSemana.isEmpty() ? null :
                historialSemana.stream().mapToInt(HistorialEstres::getPuntuacion).average().orElse(0);
        String nivelEstresPromedio = estresPromedio == null ? "" :
                estresPromedio <= 13 ? "BAJO" : estresPromedio <= 26 ? "MODERADO" : "ALTO";

        emailService.enviarResumenSemanal(u.getEmail(), u.getNombre(), completadas, pendientes,
                porcentajeMetas, estresPromedio, nivelEstresPromedio);
        log.info("Resumen semanal enviado a {} (completadas={}, pendientes={}, metas={}%, estres={})",
                u.getEmail(), completadas, pendientes, porcentajeMetas, estresPromedio);
    }
}