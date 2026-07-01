package pe.upc.equilibria.ai.gemini;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    @Value("${gemini.api-key}") private String apiKey;
    private final RestTemplate rest = new RestTemplate();
    private static final String URL = "https://openrouter.ai/api/v1/chat/completions";

    public String ask(String prompt) {
        try {
            Map<String,Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            List<Map<String,Object>> messages = new ArrayList<>();
            messages.add(message);

            Map<String,Object> body = new HashMap<>();
            body.put("model", "openrouter/auto");
            body.put("messages", messages);

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setBearerAuth(apiKey);

            Map<?,?> resp = rest.postForObject(URL, new HttpEntity<>(body, h), Map.class);
            var choices = (List<?>) resp.get("choices");
            var msg = (Map<?,?>) ((Map<?,?>) choices.get(0)).get("message");
            String content = msg.get("content").toString();

    
            content = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            return content;
        } catch (Exception e) {
            System.out.println("ERROR GEMINI: " + e.getMessage());
            return "";
        }
    }
}
