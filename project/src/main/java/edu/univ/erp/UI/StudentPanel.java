package edu.univ.erp.UI;

import edu.univ.erp.service.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.border.EmptyBorder;

public class StudentPanel extends JPanel {
    private final NotificationService notificationService = new NotificationService();
    private JButton notificationsButton;
    private JPanel notificationsPanel;
    private JList<Notification> notificationsList;
    private DefaultListModel<Notification> notificationsListModel;
    private boolean notificationsExpanded = false;
    private final StudentService studentService;
    private final UserAuth user;
    private final Student studentProfile;
    private DefaultTableModel availableSectionsModel;
    private DefaultTableModel enrolledSectionsModel;
    private DefaultTableModel timetableModel;
    private DefaultTableModel gradesModel;
    private JTable availableSectionsTable;
    private JTable enrolledSectionsTable;
    private JTable timetableTable;
    private JTable gradesTable;
    private TimetablePanel timetableGridPanel;
    private TableRowSorter<DefaultTableModel> searchSorter;
    private static final int COL_ENROLLMENT_ID = 0;
    private static final int COL_FINAL_GRADE = 5;
    public StudentPanel(UserAuth user) {
        this.user = user;
        this.studentService = new StudentService();
        this.studentProfile = studentService.getStudentProfile(user.getUserId());
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (studentProfile == null) {
            add(new JLabel("No student record found for user ID: " + user.getUserId()), BorderLayout.CENTER);
            return;
        }
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.add(createProfilePanel(), BorderLayout.NORTH);
        northWrapper.add(createNotificationsBar(), BorderLayout.SOUTH);
        add(northWrapper, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane();
        JSplitPane registrationSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createAvailableSectionsPanel(),
                createEnrolledSectionsPanel()
        );
        registrationSplit.setResizeWeight(0.5);
        tabbedPane.addTab("Registration", registrationSplit);
        tabbedPane.addTab("My Timetable", createTimetablePanel());
        tabbedPane.addTab("Academic Record", createGradesPanel());
        add(tabbedPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("📄 Download Transcript (CSV)");
        exportButton.addActionListener(e -> exportTranscript());
        bottomPanel.add(exportButton);
        add(bottomPanel, BorderLayout.SOUTH);
        refreshTables();
    }
    private JPanel createNotificationsBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));
        notificationsButton = new JButton("Notifications");
        notificationsButton.addActionListener(e -> toggleNotificationsPanel());
        panel.add(notificationsButton, BorderLayout.WEST);
        notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setVisible(false);
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
            notificationService.markAllRead(studentProfile.getUserId());
            refreshNotifications();
            toggleNotificationsPanel();
        });
        bottom.add(markAllRead);
        notificationsPanel.add(bottom, BorderLayout.SOUTH);
        panel.add(notificationsPanel, BorderLayout.CENTER);
        return panel;
    }
    private void toggleNotificationsPanel() {
        notificationsExpanded = !notificationsExpanded;
        if (notificationsExpanded) {
            refreshNotifications();
        }
        notificationsPanel.setVisible(notificationsExpanded);
        revalidate();
        repaint();
    }
    private void refreshNotifications() {
        notificationsListModel.clear();
        java.util.List<Notification> list = notificationService.getUnreadForUser(studentProfile.getUserId());
        for (Notification n : list) notificationsListModel.addElement(n);
    }
    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Student Profile"));
        profilePanel.add(new JLabel("Name: " + user.getUsername()));
        profilePanel.add(new JLabel(" | Roll No: " + studentProfile.getRollNo()));
        profilePanel.add(new JLabel(" | Program: " + studentProfile.getProgram()));
        profilePanel.add(new JLabel(" | Year: " + studentProfile.getYear()));
        return profilePanel;
    }
    private JPanel createAvailableSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Available Sections"));
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("🔍 Search Course/Instructor: "), BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        String[] cols = {"Sec. ID", "Code", "Title", "Instructor", "Day/Time", "Cap."};
        availableSectionsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        availableSectionsTable = new JTable(availableSectionsModel);
        availableSectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchSorter = new TableRowSorter<>(availableSectionsModel);
        availableSectionsTable.setRowSorter(searchSorter);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    searchSorter.setRowFilter(null);
                } else {
                    searchSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        panel.add(new JScrollPane(availableSectionsTable), BorderLayout.CENTER);
        JButton enrollButton = new JButton("Enroll in Selected Section");
        enrollButton.addActionListener(e -> handleEnroll());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(enrollButton);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createEnrolledSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("My Enrollments (Drop Classes)"));
        String[] columns = {"ID", "Code", "Title", "Status", "Instructor"};
        enrolledSectionsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        enrolledSectionsTable = new JTable(enrolledSectionsModel);
        enrolledSectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledSectionsTable.setAutoCreateRowSorter(true);
        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.addActionListener(e -> handleDrop());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(dropButton);
        panel.add(new JScrollPane(enrolledSectionsTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Weekly Schedule"));
        String[] cols = {"Course Code", "Course Title", "Day/Time", "Room No"};
        timetableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        timetableTable = new JTable(timetableModel);
        timetableTable.setRowHeight(25);
        timetableTable.setAutoCreateRowSorter(true);
        timetableGridPanel = new TimetablePanel();
        JScrollPane scroll = new JScrollPane(timetableGridPanel);
        timetableGridPanel.setPreferredSize(new Dimension(980, 680));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Academic Performance"));
        String[] cols = {"Enroll ID", "Course Code", "Title", "Instructor", "Grade Breakdown", "Grade"};
        gradesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        gradesTable = new JTable(gradesModel);
        gradesTable.setRowHeight(25);
        gradesTable.setAutoCreateRowSorter(true);
        gradesTable.getColumnModel().getColumn(COL_ENROLLMENT_ID).setMaxWidth(0);
        gradesTable.getColumnModel().getColumn(COL_ENROLLMENT_ID).setMinWidth(0);
        gradesTable.getColumnModel().getColumn(COL_ENROLLMENT_ID).setPreferredWidth(0);
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Title
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Instructor
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(450); // Breakdown
        gradesTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Grade Letter
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        setupGradesTableListener();
        return panel;
    }
    private void refreshTables() {
        loadAvailableSections();
        loadStudentData();
    }
    private void loadAvailableSections() {
        availableSectionsModel.setRowCount(0);
        List<Section> sections = studentService.getAllAvailableSections();
        for (Section s : sections) {
            Course c = studentService.getCourseById(s.getCourseId());
            Instructor i = studentService.getInstructorByInstructorId(s.getInstructorId());
            String courseCode = (c != null) ? c.getCode() : "N/A";
            String courseTitle = (c != null) ? c.getTitle() : "N/A";
            String instName = (i != null) ? i.getName() : "TBD";
            availableSectionsModel.addRow(new Object[]{
                    s.getSectionId(), courseCode, courseTitle, instName, s.getDayTime(), s.getCapacity()
            });
        }
    }
    private void loadStudentData() {
        enrolledSectionsModel.setRowCount(0);
        timetableModel.setRowCount(0);
        gradesModel.setRowCount(0);
        List<Enrollment> enrollments = studentService.getDetailedEnrollmentsByStudent(studentProfile.getUserId());
        Set<Integer> processedIds = new HashSet<>();
        List<TimetablePanel.Event> eventsForGrid = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (processedIds.contains(e.getEnrollmentId())) continue;
            processedIds.add(e.getEnrollmentId());
            String instructorName = "TBD";
            Section s = studentService.getSectionById(e.getSectionId());
            if (s != null) {
                Instructor i = studentService.getInstructorByInstructorId(s.getInstructorId());
                if (i != null) instructorName = i.getName();
            }
            List<Grade> gradeList = studentService.getGradesByEnrollmentId(e.getEnrollmentId());
            String gradeBreakdown = (gradeList == null || gradeList.isEmpty()) ?
                    "No grades yet" :
                    gradeList.stream().map(g -> g.getComponent() + ": " + g.getScore()).collect(Collectors.joining(", "));
            String letterGrade = (e.getFinalGrade() != null) ? e.getFinalGrade() : "-";
            enrolledSectionsModel.addRow(new Object[]{
                    e.getEnrollmentId(), e.getCourseCode(), e.getCourseTitle(), e.getStatus(), instructorName
            });
            if ("ENROLLED".equalsIgnoreCase(e.getStatus()) || "COMPLETED".equalsIgnoreCase(e.getStatus())) {
                timetableModel.addRow(new Object[]{
                        e.getCourseCode(), e.getCourseTitle(), e.getDayTime(), e.getRoom()
                });
                gradesModel.addRow(new Object[]{
                        e.getEnrollmentId(),
                        e.getCourseCode(),
                        e.getCourseTitle(),
                        instructorName,
                        gradeBreakdown,
                        letterGrade
                });
                String dayTime = (s != null && s.getDayTime() != null) ? s.getDayTime() : (e.getDayTime() == null ? "" : e.getDayTime());
                if (dayTime != null && !dayTime.trim().isEmpty()) {
                    List<TimetablePanel.TimeSpan> spans = TimetablePanel.parseDayTime(dayTime);
                    for (TimetablePanel.TimeSpan ts : spans) {
                        TimetablePanel.Event ev = new TimetablePanel.Event();
                        ev.sectionId = e.getSectionId();
                        ev.enrollmentId = e.getEnrollmentId();
                        ev.courseCode = e.getCourseCode();
                        ev.title = e.getCourseTitle();
                        ev.room = e.getRoom();
                        ev.dayToken = ts.day;
                        ev.start = ts.start;
                        ev.end = ts.end;
                        ev.color = TimetablePanel.colorForString(e.getCourseCode());
                        eventsForGrid.add(ev);
                    }
                }
            }
        }
        if (timetableGridPanel != null) {
            timetableGridPanel.setEvents(eventsForGrid);
        }
    }
    private void setupGradesTableListener() {
        gradesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int viewRow = gradesTable.getSelectedRow();
                    if (viewRow == -1) return;
                    int modelRow = gradesTable.convertRowIndexToModel(viewRow);
                    int enrollmentId = (int) gradesModel.getValueAt(modelRow, COL_ENROLLMENT_ID);
                    Object finalGradeValue = gradesModel.getValueAt(modelRow, COL_FINAL_GRADE);
                    List<Grade> breakdown = studentService.getGradesByEnrollmentId(enrollmentId);
                    if (breakdown.isEmpty() && "---".equals(finalGradeValue)) {
                        JOptionPane.showMessageDialog(
                                StudentPanel.this,
                                "Grade breakdown is not yet released by the instructor.",
                                "Not Released",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else if (!breakdown.isEmpty()) {
                        displayGradeBreakdown(breakdown);
                    } else {
                        JOptionPane.showMessageDialog(
                                StudentPanel.this,
                                "Final grade released. No component breakdown recorded.",
                                "Grade Breakdown",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            }
        });
    }
    private void displayGradeBreakdown(List<Grade> breakdown) {
        String[] columnNames = {"Component", "Score"};
        DefaultTableModel breakdownModel = new DefaultTableModel(columnNames, 0);
        for (Grade g : breakdown) {
            breakdownModel.addRow(new Object[]{
                    g.getComponent(),
                    String.format("%.2f", g.getScore())
            });
        }
        JTable breakdownTable = new JTable(breakdownModel);
        breakdownTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(breakdownTable);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Grade Component Breakdown",
                JOptionPane.PLAIN_MESSAGE
        );
    }
    private void handleEnroll() {
        Settings settings = studentService.getSettings();
        java.sql.Date deadline = settings.getCourseRegistrationDeadline();
        if (deadline != null) {
            java.time.LocalDate deadlineDate = deadline.toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (today.isAfter(deadlineDate)) {
                JOptionPane.showMessageDialog(
                        this,
                        "⚠Course registration deadline has passed.\nDeadline: " + deadlineDate,
                        "Deadline Passed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }
        int viewRow = availableSectionsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.");
            return;
        }
        int modelRow = availableSectionsTable.convertRowIndexToModel(viewRow);
        int sectionId = (int) availableSectionsModel.getValueAt(modelRow, 0);
        var response = studentService.enrollInSection(studentProfile.getUserId(), sectionId);
        switch (response) {
            case SUCCESS -> {
                JOptionPane.showMessageDialog(this, response.message);
                refreshTables();
            }
            case ALREADY_ENROLLED ->
                    JOptionPane.showMessageDialog(
                            this,
                            "You are already enrolled in this section.",
                            "Already Enrolled",
                            JOptionPane.WARNING_MESSAGE
                    );
            case SECTION_FULL ->
                    JOptionPane.showMessageDialog(
                            this,
                            "This section is already full.",
                            "Section Full",
                            JOptionPane.ERROR_MESSAGE
                    );
            case MAINTENANCE_MODE ->
                    JOptionPane.showMessageDialog(
                            this,
                            "System is under maintenance. Try again later.",
                            "Maintenance Mode",
                            JOptionPane.ERROR_MESSAGE
                    );
            case DATABASE_ERROR ->
                    JOptionPane.showMessageDialog(
                            this,
                            "A database error occurred.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
            default ->
                    JOptionPane.showMessageDialog(
                            this,
                            response.message,
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
        }
    }
    private void handleDrop() {
        Settings settings = studentService.getSettings();
        java.sql.Date deadline = settings.getCourseRegistrationDeadline();
        if (deadline != null) {
            java.time.LocalDate deadlineDate = deadline.toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (today.isAfter(deadlineDate)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Course droping deadline has passed.\nDeadline: " + deadlineDate,
                        "Deadline Passed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }
        int viewRow = enrolledSectionsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.");
            return;
        }
        int modelRow = enrolledSectionsTable.convertRowIndexToModel(viewRow);
        int enrollmentId = (int) enrolledSectionsModel.getValueAt(modelRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Drop this section?", "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
            var response = studentService.dropEnrollment(enrollmentId);
            if (response == StudentService.EnrollmentResponse.DROPPED) {
                JOptionPane.showMessageDialog(this, response.message);
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, response.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void exportTranscript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("transcript_" + user.getUsername() + ".csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".csv")) path += ".csv";
            String[] headers = {"Course Code", "Title", "Instructor", "Details", "Grade"};
            List<String[]> data = new java.util.ArrayList<>();
            for (int i = 0; i < gradesModel.getRowCount(); i++) {
                data.add(new String[]{
                        gradesModel.getValueAt(i, 1).toString(),
                        gradesModel.getValueAt(i, 2).toString(),
                        gradesModel.getValueAt(i, 3).toString(),
                        gradesModel.getValueAt(i, 4).toString(),
                        gradesModel.getValueAt(i, 5).toString()
                });
            }
            if (CsvExporter.exportToCsv(headers, data, path)) {
                JOptionPane.showMessageDialog(this, "Transcript saved!");
            }
        }
    }
    private static class TimetablePanel extends JPanel implements MouseListener, MouseMotionListener {
        static class Event {
            int sectionId;
            int enrollmentId;
            String courseCode;
            String title;
            String room;
            String dayToken;
            LocalTime start;
            LocalTime end;
            Color color;
            int columnIndex;
            int columnCount;
            Rectangle bounds;
        }
        static class TimeSpan {
            String day;
            LocalTime start;
            LocalTime end;
            TimeSpan(String day, LocalTime start, LocalTime end) { this.day = day; this.start = start; this.end = end; }
        }
        private LocalTime dayStart = LocalTime.of(8, 0);
        private LocalTime dayEnd = LocalTime.of(20, 0);
        private int slotMinutes = 30;
        private final List<String> daysOrder = Arrays.asList("M", "T", "W", "TH", "F");
        private List<Event> events = new ArrayList<>();
        private Event hovered = null;
        TimetablePanel() {
            setBackground(Color.WHITE);
            addMouseListener(this);
            addMouseMotionListener(this);
            setToolTipText("");
        }
        void setEvents(List<Event> evs) {
            this.events = new ArrayList<>(evs);
            computeLayoutForEvents();
            repaint();
        }
        static List<TimeSpan> parseDayTime(String dayTime) {
            if (dayTime == null) return Collections.emptyList();
            dayTime = dayTime.trim();
            if (dayTime.isEmpty()) return Collections.emptyList();
            int idx = dayTime.indexOf(' ');
            if (idx < 0) return Collections.emptyList();
            String daysToken = dayTime.substring(0, idx).toUpperCase().trim();
            String timeToken = dayTime.substring(idx + 1).trim();
            String[] parts = timeToken.split("-");
            if (parts.length != 2) return Collections.emptyList();
            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());
            List<String> days = new ArrayList<>();
            for (int i = 0; i < daysToken.length();) {
                if (i + 1 < daysToken.length() && daysToken.substring(i, i + 2).equals("TH")) {
                    days.add("TH");
                    i += 2;
                } else {
                    days.add(daysToken.substring(i, i + 1));
                    i += 1;
                }
            }
            List<TimeSpan> out = new ArrayList<>();
            for (String d : days) out.add(new TimeSpan(d, start, end));
            return out;
        }
        static Color colorForString(String s) {
            if (s == null) return new Color(120, 160, 220);
            int hash = Math.abs(s.hashCode());
            int r = 100 + (hash % 120);
            int g = 80 + ((hash / 5) % 120);
            int b = 120 + ((hash / 7) % 120);
            return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
        }
        private void computeLayoutForEvents() {
            for (Event e : events) {
                e.columnIndex = 0;
                e.columnCount = 1;
                e.bounds = null;
            }
            Map<String, List<Event>> byDay = new HashMap<>();
            for (Event e : events) {
                byDay.computeIfAbsent(e.dayToken, k -> new ArrayList<>()).add(e);
            }
            for (String day : byDay.keySet()) {
                List<Event> dayEvents = byDay.get(day);
                dayEvents.sort(Comparator.comparing((Event ev) -> ev.start).thenComparing(ev -> ev.end));
                List<LocalTime> colEndTimes = new ArrayList<>();
                for (Event ev : dayEvents) {
                    int assigned = -1;
                    for (int ci = 0; ci < colEndTimes.size(); ci++) {
                        if (!overlaps(colEndTimes.get(ci), ev.start)) {
                            assigned = ci;
                            break;
                        }
                    }
                    if (assigned == -1) {
                        colEndTimes.add(ev.end);
                        ev.columnIndex = colEndTimes.size() - 1;
                    } else {
                        ev.columnIndex = assigned;
                        colEndTimes.set(assigned, ev.end.isAfter(colEndTimes.get(assigned)) ? ev.end : colEndTimes.get(assigned));
                    }
                }
                int columnsUsed = colEndTimes.size();
                for (Event ev : dayEvents) {
                    ev.columnCount = Math.max(1, columnsUsed);
                }
            }
        }
        private boolean overlaps(LocalTime previousEnd, LocalTime start) {
            if (previousEnd == null) return false;
            return previousEnd.isAfter(start);
        }
        @Override
        public String getToolTipText(MouseEvent event) {
            Point p = event.getPoint();
            if (hovered != null && hovered.bounds != null && hovered.bounds.contains(p)) {
                Event e = hovered;
                return "<html><b>" + e.courseCode + "</b> (" + e.title + ")<br/>" +
                        e.dayToken + " " + e.start + "-" + e.end + "<br/>Room: " + (e.room == null ? "TBD" : e.room) + "</html>";
            }
            for (Event ev : events) {
                if (ev.bounds != null && ev.bounds.contains(p)) {
                    return "<html><b>" + ev.courseCode + "</b> (" + ev.title + ")<br/>" +
                            ev.dayToken + " " + ev.start + "-" + ev.end + "<br/>Room: " + (ev.room == null ? "TBD" : ev.room) + "</html>";
                }
            }
            return null;
        }
        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int leftColWidth = 70;
            int topRowHeight = 30;
            int gridW = Math.max(200, w - leftColWidth - 20);
            int gridH = Math.max(200, h - topRowHeight - 20);
            int colW = gridW / daysOrder.size();
            long totalMinutes = Duration.between(dayStart, dayEnd).toMinutes();
            double pixelsPerMinute = (double) gridH / (double) totalMinutes;
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            for (int c = 0; c < daysOrder.size(); c++) {
                int x = leftColWidth + c * colW;
                g.setColor(new Color(245, 245, 245));
                g.fillRect(x, 0, colW, topRowHeight);
                g.setColor(Color.DARK_GRAY);
                String dayName = switch (daysOrder.get(c)) {
                    case "M" -> "Mon";
                    case "T" -> "Tue";
                    case "W" -> "Wed";
                    case "TH" -> "Thu";
                    case "F" -> "Fri";
                    default -> daysOrder.get(c);
                };
                FontMetrics fm = g.getFontMetrics();
                int strw = fm.stringWidth(dayName);
                g.drawString(dayName, x + (colW - strw) / 2, 20);
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(x, 0, x, h);
            }
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(leftColWidth + daysOrder.size() * colW, 0, leftColWidth + daysOrder.size() * colW, h);
            for (long min = 0; min <= totalMinutes; min += slotMinutes) {
                int y = topRowHeight + (int) (min * pixelsPerMinute);
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(leftColWidth, y, leftColWidth + daysOrder.size() * colW, y);
                LocalTime t = dayStart.plusMinutes(min);
                String label = t.toString();
                g.setColor(Color.DARK_GRAY);
                g.drawString(label, 6, y + 12);
            }
            for (Event e : events) {
                int dayIndex = daysOrder.indexOf(e.dayToken);
                if (dayIndex < 0) continue;
                long startOffset = Duration.between(dayStart, e.start).toMinutes();
                long dur = Duration.between(e.start, e.end).toMinutes();
                int xBase = leftColWidth + dayIndex * colW;
                int slotIdx = Math.max(0, e.columnIndex);
                int slotCount = Math.max(1, e.columnCount);
                int pxX = xBase + (int) ((double) slotIdx / slotCount * colW) + 4;
                int pxW = (int) Math.max(30, (double) colW / slotCount) - 8;
                int pxY = topRowHeight + (int) (startOffset * pixelsPerMinute) + 4;
                int pxH = Math.max(18, (int) (dur * pixelsPerMinute) - 6);
                e.bounds = new Rectangle(pxX, pxY, pxW, pxH);
                g.setColor(e.color);
                g.fillRoundRect(pxX, pxY, pxW, pxH, 8, 8);
                g.setColor(Color.BLACK);
                g.drawRoundRect(pxX, pxY, pxW, pxH, 8, 8);
                String firstLine = e.courseCode == null ? e.title : e.courseCode + " - " + e.title;
                FontMetrics fm = g.getFontMetrics();
                String drawTitle = trimToWidth(firstLine, fm, pxW - 8);
                g.setClip(pxX, pxY, pxW, pxH);
                g.setColor(Color.BLACK);
                g.drawString(drawTitle, pxX + 6, pxY + 14);
                String timeStr = e.start.toString() + "-" + e.end.toString();
                String roomStr = e.room == null ? "" : " " + e.room;
                String secondLine = timeStr + roomStr;
                String drawSecond = trimToWidth(secondLine, fm, pxW - 8);
                g.drawString(drawSecond, pxX + 6, pxY + 30);
                g.setClip(null);
            }
        }
        private String trimToWidth(String s, FontMetrics fm, int width) {
            if (fm.stringWidth(s) <= width) return s;
            String ell = "...";
            int lo = 0, hi = s.length();
            while (lo < hi) {
                int mid = (lo + hi + 1) / 2;
                if (fm.stringWidth(s.substring(0, mid) + ell) <= width) lo = mid;
                else hi = mid - 1;
            }
            return s.substring(0, lo) + "...";
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
            for (Event ev : events) {
                if (ev.bounds != null && ev.bounds.contains(p)) {
                    JOptionPane.showMessageDialog(this,
                            ev.courseCode + " - " + ev.title + "\n" +
                                    "Section ID: " + ev.sectionId + "\n" +
                                    "Time: " + ev.dayToken + " " + ev.start + "-" + ev.end + "\n" +
                                    "Room: " + (ev.room == null ? "TBD" : ev.room),
                            "Section Details",
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {
            hovered = null;
            setCursor(Cursor.getDefaultCursor());
            repaint();
        }
        @Override
        public void mouseDragged(MouseEvent e) {}
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            Event found = null;
            for (Event ev : events) {
                if (ev.bounds != null && ev.bounds.contains(p)) {
                    found = ev;
                    break;
                }
            }
            if (found != hovered) {
                hovered = found;
                if (hovered != null) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        }
    }
}
