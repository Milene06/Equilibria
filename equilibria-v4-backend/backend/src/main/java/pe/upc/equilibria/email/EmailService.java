package pe.upc.equilibria.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${mail.enabled:false}") private boolean mailEnabled;
    @Value("${spring.mail.username:}") private String from;

    /**
     * PB-029 — Resumen semanal por correo.
     * Incluye (C2): tareas completadas, tareas pendientes, % cumplimiento de metas
     * y nivel de estrés promedio de la semana.
     */
    public void enviarResumenSemanal(String destino, String nombre,
                                     long completadas, long pendientes,
                                     double porcentajeMetas, Double estresPromedio,
                                     String nivelEstresPromedio) {
        if (!mailEnabled) {
            log.info("Mail deshabilitado. Resumen para {} (completadas={}, pendientes={}, metas={}%, estres={})",
                    destino, completadas, pendientes, porcentajeMetas, estresPromedio);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destino);
            helper.setSubject("Tu resumen semanal — Equilibria 📚");
            helper.setText(buildResumenHtml(nombre, completadas, pendientes,
                    porcentajeMetas, estresPromedio, nivelEstresPromedio), true);
            mailSender.send(mime);
        } catch (Exception e) { log.error("Error enviando correo a {}: {}", destino, e.getMessage()); }
    }

    private String buildResumenHtml(String nombre, long completadas, long pendientes,
                                    double porcentajeMetas, Double estresPromedio,
                                    String nivelEstresPromedio) {
        long total = completadas + pendientes;
        double porcentajeTareas = total == 0 ? 0 : (completadas * 100.0 / total);
        String estresTexto = estresPromedio == null
                ? "Sin evaluaciones esta semana"
                : String.format(Locale.US, "%.1f/40 (%s)", estresPromedio, nivelEstresPromedio);

        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f4f6fb;padding:24px;margin:0;\">"
                + "<div style=\"max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;\">"
                + "<div style=\"background:linear-gradient(135deg,#2DA39B,#1F4FA8);padding:24px;color:#fff;\">"
                + "<h1 style=\"margin:0;font-size:20px;\">📚 Tu resumen semanal</h1>"
                + "<p style=\"margin:6px 0 0;opacity:.9;font-size:13px;\">Equilibria — Bienestar + IA</p></div>"
                + "<div style=\"padding:24px;\">"
                + "<p style=\"font-size:14px;color:#333;\">Hola " + escapeHtml(nombre) + ",</p>"
                + "<p style=\"font-size:13px;color:#555;\">Aquí tienes un vistazo rápido de cómo fue tu semana en Equilibria:</p>"
                + "<table style=\"width:100%;border-collapse:collapse;margin:16px 0;\">"
                + row("✅ Tareas completadas", String.valueOf(completadas))
                + row("⏳ Tareas pendientes", String.valueOf(pendientes))
                + row("📈 Cumplimiento de tareas", String.format(Locale.US, "%.0f%%", porcentajeTareas))
                + row("🎯 Cumplimiento de metas", String.format(Locale.US, "%.0f%%", porcentajeMetas))
                + row("🧠 Nivel de estrés promedio", estresTexto)
                + "</table>"
                + "<p style=\"font-size:12px;color:#777;\">Ingresa a la plataforma para ver el detalle completo y recibir nuevas recomendaciones de tu Coach IA.</p>"
                + "<p style=\"font-size:11px;color:#999;margin-top:20px;\">¿No quieres seguir recibiendo este correo? Puedes desactivarlo desde tu Perfil → Configuración → Resumen semanal.</p>"
                + "</div>"
                + "<div style=\"background:#f4f6fb;padding:14px 24px;text-align:center;font-size:11px;color:#999;\">— Equilibria, tu asistente académico con IA</div>"
                + "</div></body></html>";
    }

    private String row(String label, String value) {
        return "<tr><td style=\"padding:8px 0;font-size:13px;color:#444;border-bottom:1px solid #f0f0f0;\">" + label + "</td>"
                + "<td style=\"padding:8px 0;font-size:13px;font-weight:700;color:#1F4FA8;text-align:right;border-bottom:1px solid #f0f0f0;\">" + value + "</td></tr>";
    }

    private String escapeHtml(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void enviarAlertaEstresAlto(String destino, String nombre, int score) {
        if (!mailEnabled) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from); msg.setTo(destino);
            msg.setSubject("⚠️ Alerta de estrés alto — Equilibria");
            msg.setText(String.format(
                    "Hola %s,\n\nTu evaluación PSS-10 reciente indica un nivel de estrés ALTO (puntuación: %d/40).\n\n" +
                            "Te recomendamos:\n• Contactar el servicio de psicología de tu universidad\n" +
                            "• Revisar las recomendaciones de Gemini en la sección de Bienestar\n" +
                            "• Usar el Coach IA para redistribuir tu carga académica\n\n" +
                            "— Equilibria", nombre, score));
            mailSender.send(msg);
        } catch (Exception e) { log.error("Error alerta estrés: {}", e.getMessage()); }
    }
}