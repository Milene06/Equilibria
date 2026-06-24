package pe.upc.equilibria.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.upc.equilibria.task.TareaRepository;
import pe.upc.equilibria.user.UsuarioRepository;
import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/storage")
@RequiredArgsConstructor
@Tag(name="Archivos", description="Archivos adjuntos a tareas (PB-033)")
public class StorageController {
    private final ArchivoTareaRepository archivoRepo;
    private final TareaRepository tareaRepo;
    private final UsuarioRepository userRepo;

    @Value("${app.storage.local-path}") private String localPath;

    @GetMapping("/tasks/{tareaId}/files")
    @Operation(summary="Listar archivos de una tarea")
    public List<ArchivoTarea> list(@PathVariable Long tareaId) {
        return archivoRepo.findByTareaIdTarea(tareaId);
    }

    @PostMapping("/tasks/{tareaId}/files")
    @Operation(summary="Subir archivo a una tarea (PB-033)")
    public ResponseEntity<?> upload(@AuthenticationPrincipal UserDetails ud,
                                    @PathVariable Long tareaId,
                                    @RequestParam("file") MultipartFile file) {
        try {
            var tarea = tareaRepo.findById(tareaId).orElseThrow();
            Path dir = Paths.get(localPath);
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path dest = dir.resolve(filename);
            Files.write(dest, file.getBytes());
            ArchivoTarea a = ArchivoTarea.builder()
                .tarea(tarea).nombreArchivo(file.getOriginalFilename())
                .urlArchivo("/data/uploads/" + filename)
                .tipo(file.getContentType()).build();
            return ResponseEntity.ok(archivoRepo.save(a));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/files/{id}")
    @Operation(summary="Eliminar archivo adjunto")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        archivoRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
