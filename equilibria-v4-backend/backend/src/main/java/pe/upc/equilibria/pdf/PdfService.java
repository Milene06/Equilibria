package pe.upc.equilibria.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.upc.equilibria.course.Curso;
import pe.upc.equilibria.task.Tarea;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service @Slf4j
public class PdfService {

    public byte[] generarPlanEstudio(String nombre, List<Tarea> tareas, List<Curso> cursos) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf);

            // Título
            doc.add(new Paragraph("Plan de Estudio — Equilibria")
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Estudiante: " + nombre)
                .setFontSize(13).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Generado: " + LocalDate.now())
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // Cursos
            doc.add(new Paragraph("Cursos del ciclo").setFontSize(14).setBold().setMarginTop(10));
            Table tc = new Table(UnitValue.createPercentArray(new float[]{40,20,20,20})).useAllAvailableWidth();
            tc.addHeaderCell(new Cell().add(new Paragraph("Nombre").setBold()));
            tc.addHeaderCell(new Cell().add(new Paragraph("Código").setBold()));
            tc.addHeaderCell(new Cell().add(new Paragraph("Créditos").setBold()));
            tc.addHeaderCell(new Cell().add(new Paragraph("Examen").setBold()));
            for (Curso c : cursos) {
                tc.addCell(c.getNombre());
                tc.addCell(c.getCodigo() != null ? c.getCodigo() : "—");
                tc.addCell(c.getCreditos() != null ? c.getCreditos().toString() : "—");
                tc.addCell(c.getFechaExamen() != null ? c.getFechaExamen().toString() : "—");
            }
            doc.add(tc);

            // Tareas
            doc.add(new Paragraph("Tareas pendientes").setFontSize(14).setBold().setMarginTop(20));
            Table tt = new Table(UnitValue.createPercentArray(new float[]{35,25,15,15,10})).useAllAvailableWidth();
            tt.addHeaderCell(new Cell().add(new Paragraph("Tarea").setBold()));
            tt.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()));
            tt.addHeaderCell(new Cell().add(new Paragraph("Prioridad").setBold()));
            tt.addHeaderCell(new Cell().add(new Paragraph("Tipo").setBold()));
            tt.addHeaderCell(new Cell().add(new Paragraph("Score IA").setBold()));
            for (Tarea t : tareas) {
                if (!t.getCompletada()) {
                    tt.addCell(t.getNombre());
                    tt.addCell(t.getFechaEntrega().toString());
                    tt.addCell(t.getPrioridad());
                    tt.addCell(t.getTipo() != null ? t.getTipo() : "—");
                    tt.addCell(t.getIaScore() != null ? t.getIaScore().toString() : "—");
                }
            }
            doc.add(tt);
            doc.add(new Paragraph("\nGenerado por Equilibria · Tesis UPC · Gemini 1.5 Pro")
                .setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(20));
            doc.close();
        } catch (Exception e) { log.error("Error generando PDF: {}", e.getMessage()); }
        return baos.toByteArray();
    }
}
