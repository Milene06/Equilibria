package pe.upc.equilibria.feedback;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.user.*;
import java.util.Map;

@RestController @RequestMapping("/feedback")
@RequiredArgsConstructor
@Tag(name="Feedback", description="Feedback sobre recomendaciones IA (PB-014)")
public class FeedbackController {
    private final FeedbackRepository feedbackRepo;
    private final UsuarioRepository userRepo;

    @PostMapping
    @Operation(summary="Registrar feedback de recomendación IA")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @RequestBody Map<String,Object> body) {
        Usuario u = userRepo.findByEmail(ud.getUsername()).orElseThrow();
        Feedback f = Feedback.builder().usuario(u)
            .util(Boolean.parseBoolean(body.getOrDefault("util","true").toString()))
            .comentario(body.getOrDefault("comentario","").toString())
            .tipo(body.getOrDefault("tipo","general").toString()).build();
        if (body.containsKey("idRecomendacion"))
            f.setIdRecomendacion(Long.parseLong(body.get("idRecomendacion").toString()));
        return ResponseEntity.ok(feedbackRepo.save(f));
    }
}
