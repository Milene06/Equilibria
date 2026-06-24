package pe.upc.equilibria.ai.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.ai.gemini.GeminiClient;
import pe.upc.equilibria.course.Curso;
import pe.upc.equilibria.task.Tarea;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ScheduleGenerator {
    private final GeminiClient gemini;

    public String generarHorario(List<Curso> cursos, List<Tarea> tareas, String perfilEstudio) {
        String cs = cursos.stream().map(c -> c.getNombre() + "(" + c.getCreditos() + " créditos)").collect(Collectors.joining(", "));
        String ts = tareas.stream().filter(t -> !t.getCompletada())
            .map(t -> t.getNombre() + " vence " + t.getFechaEntrega() + " prioridad=" + t.getPrioridad())
            .collect(Collectors.joining("; "));
        String prompt = String.format(
            "Eres ScheduleGenerator de Equilibria. Genera un horario semanal equilibrado (Lunes a Sábado) para un estudiante universitario peruano con perfil '%s'. Cursos: %s. Tareas pendientes: %s. " +
            "Incluye bloques de ESTUDIO, CLASE, DESCANSO y TIEMPO_LIBRE. Responde en formato JSON: [{dia, horaInicio, horaFin, actividad, tipo}]",
            perfilEstudio != null ? perfilEstudio : "VESPERTINO", cs, ts);
        return gemini.ask(prompt);
    }

    public String sugerirReagendado(List<Tarea> tareas, int pssScore) {
        if (pssScore < 27) return "[]";
        String ts = tareas.stream().filter(t -> !t.getCompletada())
            .map(t -> t.getNombre() + " vence=" + t.getFechaEntrega())
            .collect(Collectors.joining("; "));
        String prompt = String.format(
            "Estudiante con estrés ALTO (PSS-10=%d). Tareas: %s. " +
            "Sugiere cuáles reagendar para reducir carga. Responde en JSON: [{tarea, fechaActual, fechaSugerida, razon}]",
            pssScore, ts);
        return gemini.ask(prompt);
    }
}
