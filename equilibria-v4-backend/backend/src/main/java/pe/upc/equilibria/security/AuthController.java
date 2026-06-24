package pe.upc.equilibria.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.upc.equilibria.user.*;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name="Auth", description="Autenticación y registro de usuarios")
public class AuthController {
    private final UsuarioRepository repo;
    private final PasswordEncoder enc;
    private final JwtService jwt;
    private final AuthenticationManager authManager;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest rq) {
        if (repo.existsByEmail(rq.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
        String rol = rq.getEmail().toLowerCase().contains("admin") ? "admin" : "estudiante";
        Usuario u = Usuario.builder()
                .nombre(rq.getNombre()).email(rq.getEmail())
                .passwordHash(enc.encode(rq.getPassword())).rol(rol).build();
        repo.save(u);
        String token = jwt.generate(u.getEmail());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("nombre", u.getNombre());
        resp.put("email", u.getEmail());
        resp.put("rol", u.getRol());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest rq) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(rq.getEmail(), rq.getPassword()));
        Usuario u = repo.findByEmail(rq.getEmail()).orElseThrow();
        String token = jwt.generate(u.getEmail());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("nombre", u.getNombre());
        resp.put("email", u.getEmail());
        resp.put("rol", u.getRol());
        resp.put("id", u.getIdUsuario());
        return ResponseEntity.ok(resp);
    }

    @Data static class RegisterRequest {
        @NotBlank String nombre;
        @Email @NotBlank String email;
        @NotBlank @Size(min=6) String password;
    }
    @Data static class LoginRequest {
        @Email @NotBlank String email;
        @NotBlank String password;
    }
}
