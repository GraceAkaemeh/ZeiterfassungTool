package app;

import model.TimeEntry;
import model.User;
import service.TimeEntryService;
import pdf.PDFExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;

public class TimeTrackingGUI extends JFrame {

    private final User currentUser;
    private final TimeEntryService service = new TimeEntryService();
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TimeTrackingGUI(User currentUser) {
        this.currentUser = currentUser;

        setTitle("PKN Firma - Zeiterfassung - " + currentUser.getUsername());
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Table setup ---
        tableModel = new DefaultTableModel(new Object[]{"ID", "Beschreibung", "Start", "Ende", "Dauer", "Kategorie"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 5; // only description & category editable directly
            }
        };
        table = new JTable(tableModel);

        // --- Visual styling ---
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        refreshTable();

        // --- Buttons ---
        JButton addButton = new JButton("Eintrag hinzufügen");
        addButton.addActionListener(e -> addEntryAction());

        JButton editButton = new JButton("Bearbeiten");
        editButton.addActionListener(e -> editEntryAction());

        JButton deleteButton = new JButton("Löschen");
        deleteButton.addActionListener(e -> deleteEntryAction());

        JButton exportButton = new JButton("PDF exportieren");
        exportButton.addActionListener(e -> exportPDFAction());

        JButton logoutButton = new JButton("Abmelden");
        logoutButton.addActionListener(e -> logoutAction());

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(exportButton);
        panel.add(logoutButton);
        panel.setBackground(new Color(240, 248, 255)); // AliceBlue

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    // --- Add entry with one panel ---
    private void addEntryAction() {
        try {
            JTextField descField = new JTextField();
            String[] categories = {"Entwicklung", "Meeting", "Support", "Sonstiges"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);

            SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
            JSpinner startSpinner = new JSpinner(startModel);
            startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm"));

            SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
            JSpinner endSpinner = new JSpinner(endModel);
            endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm"));

            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.add(new JLabel("Beschreibung:"));
            panel.add(descField);
            panel.add(new JLabel("Kategorie:"));
            panel.add(categoryCombo);
            panel.add(new JLabel("Startzeit:"));
            panel.add(startSpinner);
            panel.add(new JLabel("Endzeit:"));
            panel.add(endSpinner);

            int result = JOptionPane.showConfirmDialog(this, panel, "Neuen Eintrag hinzufügen", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String description = descField.getText();
            if (description.trim().isEmpty()) return;

            String category = (String) categoryCombo.getSelectedItem();

            LocalDateTime startTime = LocalDateTime.ofInstant(((Date) startSpinner.getValue()).toInstant(), ZoneId.systemDefault());
            LocalDateTime endTime = LocalDateTime.ofInstant(((Date) endSpinner.getValue()).toInstant(), ZoneId.systemDefault());

            Duration duration = Duration.between(startTime, endTime);
            if (duration.isNegative() || duration.isZero()) {
                JOptionPane.showMessageDialog(this, "Endzeit darf nicht vor oder gleich Startzeit liegen!");
                return;
            }

            int totalMinutes = (int) duration.toMinutes();
            String formattedDuration = String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);

            TimeEntry entry = new TimeEntry();
            entry.setDescription(description);
            entry.setCategory(category);
            entry.setStartTime(startTime);
            entry.setEndTime(endTime);
            entry.setDuration(totalMinutes);
            entry.setUserId(currentUser.getId());

            if (service.addEntry(entry)) {
                JOptionPane.showMessageDialog(this, "Eintrag erfolgreich hinzugefügt!\nDauer: " + formattedDuration + " Stunden");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Hinzufügen des Eintrags.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    // --- Edit entry with one panel ---
    private void editEntryAction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Eintrag zum Bearbeiten aus.");
            return;
        }
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            JTextField descField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            String[] categories = {"Entwicklung", "Meeting", "Support", "Sonstiges"};
            JComboBox<String> categoryCombo = new JComboBox<>(categories);
            categoryCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 5));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime currentStart = LocalDateTime.parse((String) tableModel.getValueAt(selectedRow, 2), formatter);
            LocalDateTime currentEnd = LocalDateTime.parse((String) tableModel.getValueAt(selectedRow, 3), formatter);

            SpinnerDateModel startModel = new SpinnerDateModel(java.util.Date.from(currentStart.atZone(ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.MINUTE);
            JSpinner startSpinner = new JSpinner(startModel);
            startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm"));

            SpinnerDateModel endModel = new SpinnerDateModel(java.util.Date.from(currentEnd.atZone(ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.MINUTE);
            JSpinner endSpinner = new JSpinner(endModel);
            endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm"));

            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.add(new JLabel("Beschreibung:"));
            panel.add(descField);
            panel.add(new JLabel("Kategorie:"));
            panel.add(categoryCombo);
            panel.add(new JLabel("Startzeit:"));
            panel.add(startSpinner);
            panel.add(new JLabel("Endzeit:"));
            panel.add(endSpinner);

            int result = JOptionPane.showConfirmDialog(this, panel, "Eintrag bearbeiten", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            String description = descField.getText();
            if (description.trim().isEmpty()) return;

            String category = (String) categoryCombo.getSelectedItem();
            LocalDateTime startTime = LocalDateTime.ofInstant(((Date) startSpinner.getValue()).toInstant(), ZoneId.systemDefault());
            LocalDateTime endTime = LocalDateTime.ofInstant(((Date) endSpinner.getValue()).toInstant(), ZoneId.systemDefault());

            Duration duration = Duration.between(startTime, endTime);
            if (duration.isNegative() || duration.isZero()) {
                JOptionPane.showMessageDialog(this, "Endzeit darf nicht vor oder gleich Startzeit liegen!");
                return;
            }

            int totalMinutes = (int) duration.toMinutes();

            TimeEntry entry = new TimeEntry();
            entry.setId(id);
            entry.setDescription(description);
            entry.setCategory(category);
            entry.setStartTime(startTime);
            entry.setEndTime(endTime);
            entry.setDuration(totalMinutes);
            entry.setUserId(currentUser.getId());

            if (service.updateEntry(entry)) {
                JOptionPane.showMessageDialog(this, "Eintrag erfolgreich aktualisiert!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Aktualisieren des Eintrags.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    // --- Delete entry ---
    private void deleteEntryAction() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Eintrag zum Löschen aus.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Möchten Sie den ausgewählten Eintrag wirklich löschen?", "Bestätigung", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            if (service.deleteEntry(id)) {
                JOptionPane.showMessageDialog(this, "Eintrag erfolgreich gelöscht!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Fehler beim Löschen des Eintrags.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    // --- Refresh table ---
    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<TimeEntry> entries = service.getEntriesByUser(currentUser.getId());
            for (TimeEntry e : entries) {
                String durationDisplay = "";
                if (e.getDuration() > 0) {
                    long hours = e.getDuration() / 60;
                    long minutes = e.getDuration() % 60;
                    durationDisplay = String.format("%02d:%02d", hours, minutes);
                }

                tableModel.addRow(new Object[]{
                        e.getId(),
                        e.getDescription(),
                        e.getStartTime() != null ? e.getStartTime().format(dtf) : "",
                        e.getEndTime() != null ? e.getEndTime().format(dtf) : "",
                        durationDisplay,
                        e.getCategory()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Laden der Einträge: " + ex.getMessage());
        }
    }

    // --- PDF export ---
    private void exportPDFAction() {
        try {
            List<TimeEntry> entries = service.getEntriesByUser(currentUser.getId());
            if (entries.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Keine Einträge zum Exportieren vorhanden.");
                return;
            }
            String filePath = "exports/time_tracking_export.pdf";
            PDFExporter.export(entries);
            JOptionPane.showMessageDialog(this, "PDF wurde erfolgreich exportiert:\n" + filePath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim PDF-Export: " + ex.getMessage());
        }
    }

    // --- Logout ---
    private void logoutAction() {
        dispose();
        new LoginWindow().setVisible(true);
    }
}
