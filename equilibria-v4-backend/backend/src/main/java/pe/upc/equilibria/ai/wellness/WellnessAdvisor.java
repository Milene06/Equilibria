package pe.upc.equilibria.ai.wellness;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.ai.gemini.GeminiClient;

@Service @RequiredArgsConstructor
public class WellnessAdvisor {
    private final GeminiClient gemini;

    public String generarConsejos(String nombre, int score, String nivel) {
        String prompt = String.format(
            "Eres WellnessAdvisor de Equilibria. El estudiante %s obtuvo PSS-10=%d (nivel %s). " +
            "Genera 4 consejos personalizados de bienestar emocional para un estudiante universitario peruano. " +
            "Sé empático, práctico y breve. Responde en español, máximo 200 palabras.",
            nombre, score, nivel);
        return gemini.ask(prompt);
    }

    public String generarFraseMotivacional(String nombre, String nivel) {
        String prompt = String.format(
            "Genera una frase motivacional corta y poderosa para el estudiante universitario %s que tiene nivel de estrés %s. " +
            "Máximo 2 oraciones, en español, inspiradora y apropiada para su situación.",
            nombre, nivel);
        return gemini.ask(prompt);
    }

    public String recomendarTecnicas(int pssScore, int tareasCount) {
        String prompt = String.format(
            "Estudiante universitario peruano con PSS-10=%d y %d tareas pendientes. " +
            "Recomienda 3 técnicas de estudio específicas (ej. Pomodoro, mapas mentales, lectura activa) " +
            "con instrucciones concretas. En español, máximo 150 palabras.",
            pssScore, tareasCount);
        return gemini.ask(prompt);
    }
}
