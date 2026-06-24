package pe.upc.equilibria.ai.priority;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.ai.gemini.GeminiClient;
import pe.upc.equilibria.task.Tarea;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class PriorityAlgorithm {
    private final GeminiClient gemini;

    public List<Tarea> priorizar(List<Tarea> tareas) {
        String lista = tareas.stream()
            .map(t -> String.format("ID=%d nombre='%s' fecha=%s prioridad=%s dificultad=%s tipo=%s",
                t.getIdTarea(), t.getNombre(), t.getFechaEntrega(),
                t.getPrioridad(), t.getDificultad(), t.getTipo()))
            .collect(Collectors.joining("\n"));

        String prompt = "Eres PriorityAlgorithm de Equilibria. Analiza estas tareas de un estudiante universitario peruano y asigna un score de urgencia del 1 (más urgente) al 99 (menos urgente). Responde SOLO en este formato por cada tarea, una por línea:\nID=X score=Y razon=texto corto\n\nTareas:\n" + lista;

        String respuesta = gemini.ask(prompt);
        for (String linea : respuesta.split("\n")) {
            try {
                if (!linea.contains("ID=")) continue;
                Long id = Long.parseLong(linea.replaceAll(".*ID=(\\d+).*", "$1"));
                int score = Integer.parseInt(linea.replaceAll(".*score=(\\d+).*", "$1"));
                String razon = linea.replaceAll(".*razon=(.+)", "$1").trim();
                tareas.stream().filter(t -> t.getIdTarea().equals(id)).findFirst()
                    .ifPresent(t -> { t.setIaScore(score); t.setIaRazon(razon); });
            } catch (Exception ignored) {}
        }
        return tareas;
    }
}
