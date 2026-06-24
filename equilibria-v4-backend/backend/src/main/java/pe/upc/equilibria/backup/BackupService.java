package pe.upc.equilibria.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.course.CursoRepository;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.meta.MetaRepository;
import pe.upc.equilibria.user.UsuarioRepository;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class BackupService {
    private final UsuarioRepository userRepo;
    private final TareaRepository tareaRepo;
    private final CursoRepository cursoRepo;
    private final MetaRepository metaRepo;

    @Scheduled(cron = "0 0 2 * * SUN")
    public void backupSemanal() {
        File dir = new File("/data/backups");
        if (!dir.exists()) { log.info("Directorio /data/backups no existe, skip backup"); return; }
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        userRepo.findAll().forEach(u -> {
            try {
                Map<String,Object> backup = Map.of(
                    "usuario", u.getEmail(),
                    "fecha", LocalDate.now().toString(),
                    "tareas", tareaRepo.findByUsuarioIdUsuarioOrderByFechaEntregaAsc(u.getIdUsuario()),
                    "cursos", cursoRepo.findByUsuarioIdUsuarioOrderByNombreAsc(u.getIdUsuario()),
                    "metas", metaRepo.findByUsuarioIdUsuarioOrderByFechaFinAsc(u.getIdUsuario())
                );
                String path = "/data/backups/" + u.getIdUsuario() + "_" + LocalDate.now() + ".json";
                Files.writeString(Path.of(path), mapper.writeValueAsString(backup));
                log.info("Backup guardado: {}", path);
            } catch (Exception e) { log.error("Error backup usuario {}: {}", u.getEmail(), e.getMessage()); }
        });
    }
}
