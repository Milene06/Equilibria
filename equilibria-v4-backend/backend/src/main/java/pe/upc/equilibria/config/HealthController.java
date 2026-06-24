package pe.upc.equilibria.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@Tag(name="Health", description="Estado del sistema")
public class HealthController {
    @GetMapping("/health")
    @Operation(summary="Estado del servidor")
    public Map<String,Object> health() {
        return Map.of("status","UP","service","Equilibria API v4",
            "timestamp", Instant.now().toString(),
            "gemini","gemini-2.0-flash","db","PostgreSQL");
    }
}
