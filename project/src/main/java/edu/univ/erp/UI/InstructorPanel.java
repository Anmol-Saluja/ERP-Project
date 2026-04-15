package edu.univ.erp.UI;

import edu.univ.erp.domain.*;
import edu.univ.erp.service.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.border.EmptyBorder;

public class InstructorPanel extends JPanel {
    private Map<String, Integer> sectionWeightMap = new HashMap<>();
    private final NotificationService notificationService = new NotificationService();
    private JButton notificationsButton;
    private JPanel notificationsPanel;
    private JList<Notification> notificationsList;
    private DefaultListModel<Notification> notificationsListModel;
    private boolean notificationsExpanded = false;
    private final InstructorService instructorService;
    private final UserAuth user;
    private final Instructor instructor;
    private boolean isUpdatingFinalGrades = false;
    private JComboBox<Section> sectionComboBox;
    private JTable rosterTable;
    private DefaultTableModel rosterTableModel;
    private JButton saveGradesButton;
    private JButton addComponentButton;
    private JButton calculateStatsButton;
    private JPanel gradeEntryPanel;
    private JButton releaseGradesButton;
    private List<Section> sectionsTaught;
    private List<Enrollment> currentRoster;
    private Map<Integer, Map<String, Double>> gradeMap;
    private List<String> gradeComponents = new ArrayList<>();
    private static final int COL_ENROLL_ID = 0;
    private static final int FIXED_COL_COUNT = 3;
    public InstructorPanel(UserAuth user) {
        this.user = user;
        this.instructorService = new InstructorService();
        this.instructor = instructorService.getInstructorProfile(user.getUserId());
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (instructor == null) {
            add(new JLabel("Instructor record not found."), BorderLayout.CENTER);
            return;
        }
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(createTopPanel(), BorderLayout.NORTH);
        northWrapper.add(createInstructorNotificationsBar(), BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);
        add(createGradeEntryPanel(), BorderLayout.CENTER);
        loadSections();
    }
    private JPanel createInstructorNotificationsBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(6,6,6,6));
        notificationsButton = new JButton("Notifications");
        notificationsButton.addActionListener(e -> toggleInstructorNotificationsPanel());
        panel.add(notificationsButton, BorderLayout.WEST);
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setVisible(false);
        notificationsPanel.setPreferredSize(new Dimension(600, 140));
        notificationsListModel = new DefaultListModel<>();
        notificationsList = new JList<>(notificationsListModel);
        notificationsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Notification n = (Notification) value;
                String text = "<html><b>" + n.getCreatedAt() + "</b><br/>" + n.getMessage() + "</html>";
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        JScrollPane scroll = new JScrollPane(notificationsList);
        scroll.setPreferredSize(new Dimension(600, 120));
        notificationsPanel.add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markAllRead = new JButton("Mark all read");
        markAllRead.addActionListener(e -> {
            notificationService.markAllRead(instructor.getUserId());
            refreshInstructorNotifications();
            toggleInstructorNotificationsPanel();
        });
        bottom.add(markAllRead);
        notificationsPanel.add(bottom, BorderLayout.SOUTH);
        panel.add(notificationsPanel, BorderLayout.CENTER);
        return panel;
    }
    private void toggleInstructorNotificationsPanel() {
        notificationsExpanded = !notificationsExpanded;
        if (notificationsExpanded) refreshInstructorNotifications();
        notificationsPanel.setVisible(notificationsExpanded);
        revalidate();
        repaint();
    }
    private void refreshInstructorNotifications() {
        notificationsListModel.clear();
        java.util.List<Notification> list = notificationService.getUnreadForUser(instructor.getUserId());
        for (Notification n : list) notificationsListModel.addElement(n);
    }
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Instructor: " + instructor.getName()));
        infoPanel.add(new JLabel("| Department: " + instructor.getDepartment()));
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.add(new JLabel("Select Section:"));
        sectionComboBox = new JComboBox<>();
        selectorPanel.add(sectionComboBox);
        topPanel.add(infoPanel, BorderLayout.NORTH);
        topPanel.add(selectorPanel, BorderLayout.CENTER);
        sectionComboBox.addActionListener(e -> {
            Section selectedSection = (Section) sectionComboBox.getSelectedItem();
            if (selectedSection != null) {
                loadRosterForSection(selectedSection.getSectionId());
            } else {
                rosterTableModel.setRowCount(0);
                saveGradesButton.setEnabled(false);
            }
        });
        return topPanel;
    }
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        saveGradesButton = new JButton("Save All Grades for This Section");
        saveGradesButton.setBackground(new Color(60, 179, 113));
        saveGradesButton.setEnabled(false);
        saveGradesButton.addActionListener(e -> handleSaveGrades());
        controlPanel.add(saveGradesButton);
        addComponentButton = new JButton("Add New Component");
        addComponentButton.addActionListener(e -> addNewGradeComponent());
        controlPanel.add(addComponentButton);
        JButton setWeightageButton = new JButton("Set Weightage");
        setWeightageButton.addActionListener(e -> openWeightageDialog());
        controlPanel.add(setWeightageButton);
        calculateStatsButton = new JButton("Generate Class Statistics");
        calculateStatsButton.addActionListener(e -> calculateStatsAndFinalGrades());
        controlPanel.add(calculateStatsButton);
        JButton importGradesButton = new JButton("Import Grades CSV");
        importGradesButton.addActionListener(e -> importGradesCsv());
        controlPanel.add(importGradesButton);
        releaseGradesButton = new JButton("Release Final Grades");
        releaseGradesButton.setBackground(new Color(220, 20, 60));
        releaseGradesButton.setEnabled(false);
        releaseGradesButton.addActionListener(e -> releaseGradesAction());
        controlPanel.add(releaseGradesButton);
        JButton exportGradesButton = new JButton("Download Grades (CSV)");
        exportGradesButton.addActionListener(e -> exportGradesCsv());
        controlPanel.add(exportGradesButton);
        return controlPanel;
    }
    private void openWeightageDialog() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Select a section first.");
            return;
        }
        Map<String,Integer> weightMap = instructorService.getSectionWeightage(selectedSection.getSectionId());
        JPanel panel = new JPanel(new GridLayout(weightMap.size(), 2, 10, 10));
        Map<String, JTextField> fields = new HashMap<>();
        for (String comp : gradeComponents) {
            panel.add(new JLabel(comp + " Weight:"));
            JTextField tf = new JTextField(weightMap.getOrDefault(comp, 0).toString());
            fields.put(comp, tf);
            panel.add(tf);
        }
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Set Weightage for Section " + selectedSection.getSectionId(),
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Map<String,Integer> updated = new HashMap<>();
            for (String comp : gradeComponents) {
                try {
                    updated.put(comp, Integer.parseInt(fields.get(comp).getText().trim()));
                } catch (Exception ex) {
                    updated.put(comp, 0);
                }
            }
            if (instructorService.saveSectionWeightage(selectedSection.getSectionId(), updated)) {
                JOptionPane.showMessageDialog(this, "Weightage updated!");
                loadRosterForSection(selectedSection.getSectionId());
            } else {
                if (AccessChecker.isMaintenanceMode()) {
                    JOptionPane.showMessageDialog(this,
                            "Changes cannot be made while in maintenance mode.",
                            "Action Blocked", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update weightage due to an unexpected error.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private JPanel createGradeEntryPanel() {
        gradeEntryPanel = new JPanel(new BorderLayout(10, 10));
        gradeEntryPanel.setBorder(BorderFactory.createTitledBorder("Grade Roster"));
        String[] columnNames = {"Enroll ID", "Roll No", "Student Name"};
        rosterTableModel = new DefaultTableModel(columnNames, 0);
        rosterTable = new JTable(rosterTableModel);
        gradeEntryPanel.add(new JScrollPane(rosterTable), BorderLayout.CENTER);
        gradeEntryPanel.add(createControlPanel(), BorderLayout.SOUTH);
        return gradeEntryPanel;
    }
    private void releaseGradesAction() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stopCellEditing();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you SURE you want to release the final grades for Section ID:" + selectedSection.getSectionId() + "\nThis action cannot be undone and will make all grades visible to students.",
                "Confirm Grade Release",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            int sectionId = selectedSection.getSectionId();
            if (instructorService.releaseGrades(sectionId)) {
                JOptionPane.showMessageDialog(this, "✅ Grades for Section " + sectionId +
                        " have been successfully released!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRosterForSection(sectionId);
            } else {
                if (AccessChecker.isMaintenanceMode()) {
                    JOptionPane.showMessageDialog(this,
                            "Changes cannot be made while in maintenance mode.",
                            "Action Blocked", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to release grades. Check server logs or verify ownership.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void loadSections() {
        sectionsTaught = instructorService.getSectionsByInstructor(instructor.getInstructorId());
        sectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Section) {
                    Section s = (Section) value;
                    setText(String.format("Sec %d (Course %d) - %s %d", s.getSectionId(), s.getCourseId(), s.getSemester(), s.getYear()));
                } else if (value == null) {
                    setText("Select a section...");
                }
                return this;
            }
        });
        sectionComboBox.addItem(null);
        for (Section s : sectionsTaught) {
            sectionComboBox.addItem(s);
        }
    }
    private void loadRosterForSection(int sectionId) {
        stopCellEditing();
        sectionWeightMap = instructorService.getSectionWeightage(sectionId);
        rosterTable.setModel(new DefaultTableModel());
        currentRoster = instructorService.getEnrollmentsBySection(sectionId);
        gradeMap = instructorService.getGradesForSection(sectionId);
        List<String> dbComponents = instructorService.getGradeComponentsForSection(sectionId);
        this.gradeComponents = new ArrayList<>(dbComponents);
        gradeComponents.removeIf(c ->
                c.equalsIgnoreCase("Final Score") ||
                        c.equalsIgnoreCase("Final") ||
                        c.equalsIgnoreCase("FinalScore") ||
                        c.equalsIgnoreCase("Score") ||
                        c.trim().isEmpty()
        );
        List<String> dynamicHeaders = new ArrayList<>();
        dynamicHeaders.add("Enroll ID");
        dynamicHeaders.add("Roll No");
        dynamicHeaders.add("Student Name");
        for (String comp : gradeComponents) {
            dynamicHeaders.add(comp + " Score");
        }
        dynamicHeaders.add("Final Score");
        dynamicHeaders.add("Grade");
        rosterTableModel = new DefaultTableModel(dynamicHeaders.toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= FIXED_COL_COUNT && column < FIXED_COL_COUNT + gradeComponents.size();
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (isUpdatingFinalGrades) {
                    super.setValueAt(aValue, row, column);
                    return;
                }
                super.setValueAt(aValue, row, column);
                if (isCellEditable(row, column)) {
                    updateFinalGradesForSingleRow(row);
                }
            }
        };
        rosterTable.setModel(rosterTableModel);

        for (int i = 0; i < gradeComponents.size(); i++) {
            int scoreCol = FIXED_COL_COUNT + i;

            javax.swing.text.JTextComponent editorField = new javax.swing.JTextField();
            ((javax.swing.text.AbstractDocument) editorField.getDocument())
                    .setDocumentFilter(new NumericDocumentFilter());

            rosterTable.getColumnModel().getColumn(scoreCol)
                    .setCellEditor(new DefaultCellEditor((JTextField) editorField));
        }
        rosterTable.getColumnModel().getColumn(COL_ENROLL_ID).setMinWidth(0);
        rosterTable.getColumnModel().getColumn(COL_ENROLL_ID).setMaxWidth(0);
        rosterTable.getColumnModel().getColumn(COL_ENROLL_ID).setWidth(0);
        for (Enrollment e : currentRoster) {
            List<Object> row = new ArrayList<>();
            row.add(e.getEnrollmentId());
            row.add(e.getRollNo());
            row.add(e.getStudentName());
            Map<String, Double> gradesForEnrollment = gradeMap.getOrDefault(e.getEnrollmentId(), Collections.emptyMap());
            for (String component : gradeComponents) {
                row.add(gradesForEnrollment.get(component));
            }
            Map<String, Integer> weightMapForStudent = sectionWeightMap;
            String finalGrade = instructorService.computeFinalGrade(gradesForEnrollment, weightMapForStudent);
            double weightedTotal = 0;
            int totalWeights = 0;
            for (String comp : gradesForEnrollment.keySet()) {
                double s = gradesForEnrollment.get(comp);
                int w = weightMapForStudent.getOrDefault(comp, 0);
                weightedTotal += s * w;
                totalWeights += w;
            }
            double finalScore = (totalWeights == 0 ? 0 : weightedTotal / totalWeights);
            row.add(String.format("%.2f", finalScore));
            row.add(finalGrade);
            rosterTableModel.addRow(row.toArray());
        }
        saveGradesButton.setEnabled(true);
        releaseGradesButton.setEnabled(true);
    }
    private void updateFinalGradesForSingleRow(int row) {
        Map<String, Double> gradesForStudent = new HashMap<>();
        Map<String, Integer> weightMapForStudent = sectionWeightMap;
        for (int i = 0; i < gradeComponents.size(); i++) {
            int scoreColIndex = FIXED_COL_COUNT + i;
            String componentName = gradeComponents.get(i);
            Double score = parseGrade(rosterTableModel.getValueAt(row, scoreColIndex));
            Integer weight = sectionWeightMap.getOrDefault(componentName, 0);
            if (score != null) {
                gradesForStudent.put(componentName, score);
                weightMapForStudent.put(componentName, weight);
            }
        }
        double weightedTotal = 0;
        int totalWeights = 0;
        for (String comp : gradesForStudent.keySet()) {
            double s = gradesForStudent.get(comp);
            int w = weightMapForStudent.getOrDefault(comp, 0);
            weightedTotal += s * w;
            totalWeights += w;
        }
        double finalScore = (totalWeights == 0 ? 0 : weightedTotal / totalWeights);
        String finalGrade = instructorService.computeFinalGrade(gradesForStudent, weightMapForStudent);
        int finalScoreCol = rosterTableModel.getColumnCount() - 2;
        int gradeCol = rosterTableModel.getColumnCount() - 1;
        isUpdatingFinalGrades = true;
        rosterTableModel.setValueAt(String.format("%.2f", finalScore), row, finalScoreCol);
        rosterTableModel.setValueAt(finalGrade, row, gradeCol);
        isUpdatingFinalGrades = false;
    }
    private void handleSaveGrades() {
        stopCellEditing();
        List<Grade> gradesToSave = new ArrayList<>();
        boolean success = true;
        for (int row = 0; row < rosterTableModel.getRowCount(); row++) {
            int enrollmentId = (int) rosterTableModel.getValueAt(row, COL_ENROLL_ID);
            Map<String, Double> gradesForStudent = new HashMap<>();
            Map<String, Integer> weightMapForStudent = sectionWeightMap;
            for (int i = 0; i < gradeComponents.size(); i++) {
                int scoreColIndex = FIXED_COL_COUNT + i ;
                String componentName = gradeComponents.get(i);
                Double score = parseGrade(rosterTableModel.getValueAt(row, scoreColIndex));
                int weight = sectionWeightMap.getOrDefault(componentName, 0);
                if (score != null) {
                    Grade g = new Grade();
                    g.setEnrollmentId(enrollmentId);
                    g.setComponent(componentName);
                    g.setScore(score);
                    g.setWeightage(weight);
                    gradesToSave.add(g);
                    gradesForStudent.put(componentName, score);
                    weightMapForStudent.put(componentName, weight);
                }
            }
            String finalGrade = instructorService.computeFinalGrade(gradesForStudent, weightMapForStudent);
            System.out.println("Student ID: " + enrollmentId + " | Calculated Final Grade: " + finalGrade);
            if (!instructorService.updateFinalGrade(enrollmentId, finalGrade)) {
                success = false;
                System.err.println(" FAILED to update DB for ID: " + enrollmentId);
            } else {
                System.out.println("Database Updated successfully for ID: " + enrollmentId);
            }
        }
        if (instructorService.saveGrades(gradesToSave) && success) {
            JOptionPane.showMessageDialog(this, "Grades Saved & Calculated!");
        } else {
            if (AccessChecker.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this,
                        "Changes cannot be made while in maintenance mode.",
                        "Action Blocked", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error saving data. Check console.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void addNewGradeComponent() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (AccessChecker.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Changes cannot be made while in maintenance mode.",
                    "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newComponentName = JOptionPane.showInputDialog(
                this,
                "Enter the name for the new grade component (e.g., Quiz 1, Essay):",
                "Add New Component",
                JOptionPane.PLAIN_MESSAGE
        );
        if (newComponentName == null || newComponentName.trim().isEmpty()) {
            return;
        }
        newComponentName = newComponentName.trim();
        if (newComponentName.equalsIgnoreCase("Final Score") || newComponentName.equalsIgnoreCase("Grade")) {
            JOptionPane.showMessageDialog(this, "Component name is reserved.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (gradeComponents.contains(newComponentName)) {
            JOptionPane.showMessageDialog(this, "Component name already exists in this section.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gradeComponents.add(newComponentName);
        sectionWeightMap.put(newComponentName, 0);
        int sectionId = ((Section)sectionComboBox.getSelectedItem()).getSectionId();
        for (Enrollment e : currentRoster) {
            Grade g = new Grade();
            g.setEnrollmentId(e.getEnrollmentId());
            g.setComponent(newComponentName);
            g.setScore(0.0);
            g.setWeightage(0);
            instructorService.saveGrades(List.of(g));
        }
        instructorService.saveSectionWeightage(sectionId, sectionWeightMap);
        sectionWeightMap.put(newComponentName, 0);
        loadRosterForSection(selectedSection.getSectionId());
        JOptionPane.showMessageDialog(
                this,
                "Added new component '" + newComponentName + "' with weight 0"+
                        ". Enter scores and click Save Grades to store to database.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    private void calculateStatsAndFinalGrades() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null || rosterTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Load a section with students first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stopCellEditing();
        loadRosterForSection(selectedSection.getSectionId());
        StringBuilder statsReport = new StringBuilder("Class Statistics for " + selectedSection.getCourseCode() + "\n\n");
        List<Double> finalScores = new ArrayList<>();
        for (String component : gradeComponents) {
            Map<String, Double> stats = instructorService.calculateComponentStatistics(selectedSection.getSectionId(), component);
            if (stats != null && !stats.isEmpty()) {
                statsReport.append("--- ").append(component).append(" ---\n");
                statsReport.append(String.format("  Highest: %.2f\n", stats.getOrDefault("Max", 0.0)));
                statsReport.append(String.format("  Lowest: %.2f\n", stats.getOrDefault("Min", 0.0)));
                statsReport.append(String.format("  Average: %.2f\n", stats.getOrDefault("Avg", 0.0)));
                statsReport.append(String.format("  Median: %.2f\n", stats.getOrDefault("Median", 0.0)));
            }
        }
        int finalScoreCol = rosterTableModel.getColumnCount() - 2;
        for (int i = 0; i < rosterTableModel.getRowCount(); i++) {
            Object scoreValue = rosterTableModel.getValueAt(i, finalScoreCol);
            if (scoreValue != null) {
                try {
                    finalScores.add(Double.parseDouble(scoreValue.toString()));
                } catch (NumberFormatException ignored) {}
            }
        }
        if (!finalScores.isEmpty()) {
            double sum = finalScores.stream().mapToDouble(Double::doubleValue).sum();
            double min = finalScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = finalScores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double avg = sum / finalScores.size();
            Collections.sort(finalScores);
            double median;
            int size = finalScores.size();
            if (size % 2 == 1) {
                median = finalScores.get(size / 2);
            } else {
                median = (finalScores.get(size / 2 - 1) + finalScores.get(size / 2)) / 2.0;
            }
            statsReport.append("\n============================\n");
            statsReport.append("OVERALL FINAL SCORE STATISTICS\n");
            statsReport.append(String.format("  Highest: %.2f\n", max));
            statsReport.append(String.format("  Lowest: %.2f\n", min));
            statsReport.append(String.format("  Average: %.2f\n", avg));
            statsReport.append(String.format("  Median: %.2f\n", median));
        }
        JTextArea textArea = new JTextArea(statsReport.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Class Statistics & Final Grades", JOptionPane.INFORMATION_MESSAGE);
    }
    private Double parseGrade(Object value) {
        if (value == null) return null;
        try {
            if (value.toString().trim().isEmpty()) return null;
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private void stopCellEditing() {
        if (rosterTable.isEditing()) {
            TableCellEditor editor = rosterTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }
    private void importGradesCsv() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        int confirm = JOptionPane.showConfirmDialog(this, "Importing will update grades for section " + selectedSection.getSectionId() + ". Continue?", "Confirm Import", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            edu.univ.erp.util.ImportResult result = instructorService.importGradeRosterFromFile(selectedSection.getSectionId(), src);
            if (result != null && result.hasMissingRolls()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Warning: The following roll numbers from the CSV were not found in the database and were skipped:\n\n");
                int count = 0;
                for (String r : result.getMissingRolls()) {
                    sb.append(r).append(", ");
                    if (++count % 8 == 0) sb.append("\n");
                }
                String message = sb.toString().trim();
                if (message.endsWith(",")) message = message.substring(0, message.length()-1);
                JOptionPane.showMessageDialog(this, message, "Missing Students", JOptionPane.WARNING_MESSAGE);
            }
            int changed = result == null ? 0 : result.getRowsChanged();
            JOptionPane.showMessageDialog(this, "Import completed. Records updated/inserted: " + changed);
            loadRosterForSection(selectedSection.getSectionId()); // refresh view
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void exportGradesCsv() {
        Section selectedSection = (Section) sectionComboBox.getSelectedItem();
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int sectionId = selectedSection.getSectionId();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("grades_section_" + sectionId + ".csv"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = fileChooser.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".csv")) dest = new File(dest.getAbsolutePath() + ".csv");
        try {
            instructorService.exportGradeRosterForSection(sectionId, dest.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Grades exported successfully to:\n" + dest.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    class NumericDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {
            if (string == null) return;
            if (string.matches("[0-9.]*")) {
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                throws javax.swing.text.BadLocationException {
            if (text == null) return;
            if (text.matches("[0-9.]*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}