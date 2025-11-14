package pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import model.TimeEntry;

import javax.swing.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void export(List<TimeEntry> entries) {
        try {
            // Ask user where to save PDF
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF");
            fileChooser.setSelectedFile(new File("TimeEntries.pdf"));
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return; // user canceled
            }

            File fileToSave = fileChooser.getSelectedFile();

            // Setup PDF writer and document
            PdfWriter writer = new PdfWriter(fileToSave.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Optional: set font
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            document.setFont(font);

            // Create table with 6 columns
            Table table = new Table(6);

            table.addHeaderCell(new Cell().add(new Paragraph("ID der Aufgabe")));
            table.addHeaderCell(new Cell().add(new Paragraph("Start Zeit")));
            table.addHeaderCell(new Cell().add(new Paragraph("Ende Zeit")));
            table.addHeaderCell(new Cell().add(new Paragraph("Dauer (Minuten)")));
            table.addHeaderCell(new Cell().add(new Paragraph("Kategory")));
            table.addHeaderCell(new Cell().add(new Paragraph("Beschreibung")));

            // Add all entries
            for (TimeEntry e : entries) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(e.getId()))));
                table.addCell(new Cell().add(new Paragraph(
                        e.getStartTime() != null ? e.getStartTime().format(dtf) : ""
                )));
                table.addCell(new Cell().add(new Paragraph(
                        e.getEndTime() != null ? e.getEndTime().format(dtf) : ""
                )));
                table.addCell(new Cell().add(new Paragraph(
                        String.valueOf(e.getDuration())
                )));
                table.addCell(new Cell().add(new Paragraph(
                        e.getCategory() != null ? e.getCategory() : ""
                )));
                table.addCell(new Cell().add(new Paragraph(
                        e.getDescription() != null ? e.getDescription() : ""
                )));
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(null, "PDF exported successfully to:\n" + fileToSave.getAbsolutePath());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error exporting PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
