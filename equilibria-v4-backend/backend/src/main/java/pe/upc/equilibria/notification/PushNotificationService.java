package pe.upc.equilibria.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.preference.PreferenciaRepository;
import java.time.LocalTime;

@Service @RequiredArgsConstructor
public class PushNotificationService {
    private final PreferenciaRepository prefRepo;
    @Value("${app.push.enabled}") private boolean pushEnabled;

    public boolean puedeEnviar(Long uid) {
        if (!pushEnabled) return false;
        // Verificar modo descanso
        String descanso = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "modo_descanso")
            .map(p -> p.getValor()).orElse("false");
        if ("true".equals(descanso)) return false;
        // Verificar horario de descanso
        try {
            String inicio = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "descanso_inicio")
                .map(p -> p.getValor()).orElse(null);
            String fin = prefRepo.findByUsuarioIdUsuarioAndClave(uid, "descanso_fin")
                .map(p -> p.getValor()).orElse(null);
            if (inicio != null && fin != null) {
                LocalTime ahora = LocalTime.now();
                LocalTime hi = LocalTime.parse(inicio);
                LocalTime hf = LocalTime.parse(fin);
                if (hi.isAfter(hf)) {
                    if (ahora.isAfter(hi) || ahora.isBefore(hf)) return false;
                } else {
                    if (ahora.isAfter(hi) && ahora.isBefore(hf)) return false;
                }
            }
        } catch (Exception ignored) {}
        return true;
    }
}
