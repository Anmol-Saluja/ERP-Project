package edu.univ.erp.UI;

import edu.univ.erp.service.*;
import edu.univ.erp.domain.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AdminPanel extends JPanel {
    private final AdminService adminService = new AdminService();
    private final RestoreService rs = new RestoreService();
    private final UserAuth user;
    private DefaultTableModel usersTableModel;
    private DefaultTableModel studentsTableModel;
    private DefaultTableModel instructorsTableModel;
    private DefaultTableModel coursesTableModel;
    private DefaultTableModel sectionsTableModel;
    private JTable usersTable, studentsTable, instructorsTable, coursesTable, sectionsTable;
    public AdminPanel(UserAuth user) {
        this.user = user;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel header = new JLabel("Admin Dashboard - " + user.getUsername(), SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Users", createUsersTab());
        tabbedPane.addTab("Profiles", createProfilesTab());
        tabbedPane.addTab("Courses & Sections", createCoursesTab());
        tabbedPane.addTab("Settings", createSettingsTab());
        add(tabbedPane, BorderLayout.CENTER);
        refreshAllTables();
    }
    private JComponent createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        usersTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Status"}, 0);
        usersTable = new JTable(usersTableModel);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New User (Auth DB)"));
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        gridPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(15);
        gridPanel.add(usernameField);
        gridPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(15);
        gridPanel.add(passwordField);
        gridPanel.add(new JLabel("Role:"));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR", "ADMIN"});
        gridPanel.add(roleCombo);
        formPanel.add(gridPanel, BorderLayout.CENTER);
        JButton createUserButton = new JButton("Create User");
        formPanel.add(createUserButton, BorderLayout.SOUTH);
        createUserButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleCombo.getSelectedItem();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            UserAuth newUser = adminService.createNewUser(username, password, role);
            if (newUser != null) {
                JOptionPane.showMessageDialog(this, "User " + username + " created (ID: " + newUser.getUserId() + ").\n" +
                        "Please go to the 'Profiles' tab to create their Student/Instructor profile.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllTables();
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Could not create user (username may be taken).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JComponent createProfilesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JSplitPane tableSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tableSplit.setResizeWeight(0.5);
        studentsTableModel = new DefaultTableModel(new String[]{"User ID", "Roll No", "Program", "Year"}, 0);
        studentsTable = new JTable(studentsTableModel);
        JScrollPane studentScroll = new JScrollPane(studentsTable);
        studentScroll.setBorder(BorderFactory.createTitledBorder("Student Profiles (ERP DB)"));
        tableSplit.setLeftComponent(studentScroll);
        instructorsTableModel = new DefaultTableModel(new String[]{"Instr ID", "User ID", "Name", "Email", "Dept"}, 0);
        instructorsTable = new JTable(instructorsTableModel);
        JScrollPane instrScroll = new JScrollPane(instructorsTable);
        instrScroll.setBorder(BorderFactory.createTitledBorder("Instructor Profiles (ERP DB)"));
        tableSplit.setRightComponent(instrScroll);
        panel.add(tableSplit, BorderLayout.CENTER);
        JTabbedPane formTabs = new JTabbedPane();
        formTabs.addTab("Create Student Profile", createStudentProfileForm());
        formTabs.addTab("Create Instructor Profile", createInstructorProfileForm());
        panel.add(formTabs, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createStudentProfileForm() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Link User ID to Student Profile"));
        panel.add(new JLabel("User ID (from Users tab):"));
        JTextField userIdField = new JTextField(5);
        panel.add(userIdField);
        panel.add(new JLabel("Roll No:"));
        JTextField rollNoField = new JTextField(15);
        panel.add(rollNoField);
        panel.add(new JLabel("Program (e.g., B.Tech CSE):"));
        JTextField programField = new JTextField(15);
        panel.add(programField);
        panel.add(new JLabel("Year:"));
        JTextField yearField = new JTextField(5);
        panel.add(yearField);
        JButton createButton = new JButton("Create Student Profile");
        panel.add(new JLabel());
        panel.add(createButton);
        createButton.addActionListener(e -> {
            try {
                Student s = new Student();
                s.setUserId(Integer.parseInt(userIdField.getText().trim()));
                s.setRollNo(rollNoField.getText().trim());
                s.setProgram(programField.getText().trim());
                s.setYear(Integer.parseInt(yearField.getText().trim()));
                if (adminService.createStudentProfile(s)) {
                    JOptionPane.showMessageDialog(this, "Student profile created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllTables();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "User ID and Year must be numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate entry") || ex.getSQLState().startsWith("23")) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to create profile: That User ID or Roll No is already in use.",
                            "Duplicate Data Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Database Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        });
        return panel;
    }
    private JPanel createInstructorProfileForm() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Link User ID to Instructor Profile"));
        panel.add(new JLabel("User ID (from Users tab):"));
        JTextField userIdField = new JTextField(5);
        panel.add(userIdField);
        panel.add(new JLabel("Full Name:"));
        JTextField nameField = new JTextField(15);
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(15);
        panel.add(emailField);
        panel.add(new JLabel("Department:"));
        JTextField deptField = new JTextField(15);
        panel.add(deptField);
        JButton createButton = new JButton("Create Instructor Profile");
        panel.add(new JLabel());
        panel.add(createButton);
        createButton.addActionListener(e -> {
            try {
                Instructor i = new Instructor();
                i.setUserId(Integer.parseInt(userIdField.getText().trim()));
                i.setName(nameField.getText().trim());
                i.setEmail(emailField.getText().trim());
                i.setDepartment(deptField.getText().trim());
                if (adminService.createInstructorProfile(i)) {
                    JOptionPane.showMessageDialog(this, "Instructor profile created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllTables();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create profile (check User ID or Email).", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "User ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    private JComponent createCoursesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JSplitPane tableSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tableSplit.setResizeWeight(0.5);
        coursesTableModel = new DefaultTableModel(new String[]{"ID", "Code", "Title", "Credits"}, 0);
        coursesTable = new JTable(coursesTableModel);
        JScrollPane courseScroll = new JScrollPane(coursesTable);
        courseScroll.setBorder(BorderFactory.createTitledBorder("All Courses"));
        tableSplit.setLeftComponent(courseScroll);
        sectionsTableModel = new DefaultTableModel(new String[]{"ID", "Course ID", "Instr ID", "Day/Time", "Room", "Cap"}, 0);
        sectionsTable = new JTable(sectionsTableModel);
        JScrollPane sectionScroll = new JScrollPane(sectionsTable);
        sectionScroll.setBorder(BorderFactory.createTitledBorder("All Sections"));
        tableSplit.setRightComponent(sectionScroll);
        JPanel sectionPanel = new JPanel(new BorderLayout(5, 5));
        sectionPanel.add(sectionScroll, BorderLayout.CENTER);
        JButton deleteSectionButton = new JButton("Delete Selected Section");
        deleteSectionButton.addActionListener(e -> deleteSectionAction());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteSectionButton);
        sectionPanel.add(buttonPanel, BorderLayout.SOUTH);
        tableSplit.setRightComponent(sectionPanel);
        panel.add(tableSplit, BorderLayout.CENTER);
        JTabbedPane formTabs = new JTabbedPane();
        formTabs.addTab("Create Course", createCourseForm());
        formTabs.addTab("Create Section", createSectionForm());
        panel.add(formTabs, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createCourseForm() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        panel.add(new JLabel("Code:"));
        JTextField codeField = new JTextField(8);
        panel.add(codeField);
        panel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(20);
        panel.add(titleField);
        panel.add(new JLabel("Credits:"));
        JTextField creditsField = new JTextField(3);
        panel.add(creditsField);
        JButton createButton = new JButton("Create Course");
        panel.add(createButton);
        createButton.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String title = titleField.getText().trim();
                int credits = Integer.parseInt(creditsField.getText().trim());
                Course course = new Course(0, code, title, credits);
                if (adminService.createNewCourse(course)) {
                    JOptionPane.showMessageDialog(this, "Course created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllTables();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create course. Code might be a duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credits must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    private JPanel createSectionForm() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Create New Section"));
        panel.add(new JLabel("Course ID:"));
        JTextField courseIdField = new JTextField(5);
        panel.add(courseIdField);
        panel.add(new JLabel("Instructor ID:"));
        JTextField instIdField = new JTextField(5);
        panel.add(instIdField);
        panel.add(new JLabel("Day/Time (e.g., MWF 10:00-11:30) (T=Tuesday, TH=Thursday, time is in 24 hour format):"));
        JTextField timeField = new JTextField(15);
        panel.add(timeField);
        panel.add(new JLabel("Room:"));
        JTextField roomField = new JTextField(10);
        panel.add(roomField);
        panel.add(new JLabel("Capacity:"));
        JTextField capField = new JTextField(5);
        panel.add(capField);
        panel.add(new JLabel("Semester (e.g., Fall):"));
        JTextField semField = new JTextField(10);
        panel.add(semField);
        panel.add(new JLabel("Year (e.g., 2025):"));
        JTextField yearField = new JTextField(5);
        panel.add(yearField);
        JButton createButton = new JButton("Create Section");
        panel.add(new JLabel());
        panel.add(createButton);
        createButton.addActionListener(e -> {
            try {
                int capacity;
                try {
                    capacity = Integer.parseInt(capField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Capacity must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (capacity < 0) {
                    JOptionPane.showMessageDialog(this, "Capacity cannot be negative.", "Invalid Capacity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Section section = new Section();
                section.setCourseId(Integer.parseInt(courseIdField.getText().trim()));
                section.setInstructorId(Integer.parseInt(instIdField.getText().trim()));
                section.setDayTime(timeField.getText().trim());
                section.setRoom(roomField.getText().trim());
                section.setCapacity(capacity);
                section.setSemester(semField.getText().trim());
                section.setYear(Integer.parseInt(yearField.getText().trim()));
                if (adminService.createNewSection(section)) {
                    JOptionPane.showMessageDialog(this, "Section created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllTables();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create section. Check if Course/Instructor IDs are valid.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "IDs, Capacity, and Year must be numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }
    private JPanel createSettingsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        Settings currentSettings = adminService.getSettings();
        JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox maintenanceCheck = new JCheckBox("Enable Maintenance Mode");
        maintenanceCheck.setFont(new Font("Segoe UI", Font.BOLD, 16));
        maintenanceCheck.setSelected(currentSettings.isMaintenanceMode());
        maintenanceCheck.addActionListener(e -> {
            boolean isEnabled = maintenanceCheck.isSelected();
            boolean success = adminService.updateMaintenanceMode(isEnabled);
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Maintenance Mode " + (isEnabled ? "ENABLED" : "DISABLED"),
                        "Settings Updated",
                        JOptionPane.INFORMATION_MESSAGE
                );
                updateDashboardBanner();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Error: Could not update settings.",
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                maintenanceCheck.setSelected(!isEnabled);
            }
        });
        maintenancePanel.add(maintenanceCheck);
        panel.add(maintenancePanel);
        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel deadlineLabel = new JLabel("Course Registration Deadline:");
        deadlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deadlinePanel.add(deadlineLabel);
        java.util.Date currentDeadline =
                currentSettings.getCourseRegistrationDeadline() != null
                        ? new java.util.Date(currentSettings.getCourseRegistrationDeadline().getTime())
                        : new java.util.Date();
        SpinnerDateModel dateModel = new SpinnerDateModel(
                currentDeadline,
                null,
                null,
                java.util.Calendar.DAY_OF_MONTH
        );
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        deadlinePanel.add(dateSpinner);
        JButton updateDeadlineButton = new JButton("Update Deadline");
        updateDeadlineButton.addActionListener(e -> {
            java.util.Date newDeadline = (java.util.Date) dateSpinner.getValue();
            boolean success = adminService.updateRegistrationDeadline(new java.sql.Date(newDeadline.getTime()));
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Registration Deadline Updated!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update deadline.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        deadlinePanel.add(updateDeadlineButton);
        panel.add(deadlinePanel);
        JButton backupButton = new JButton("Backup All Databases");
        backupButton.addActionListener(e -> {
            boolean ok = adminService.backupAllDatabases();
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Backup completed successfully!",
                        "Backup Done",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Backup FAILED. Check console for details.",
                        "Backup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton restoreButton = new JButton("Restore Database");
        restoreButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select SQL Backup File (.sql)");
            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;
            File sqlFile = chooser.getSelectedFile();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Restoring will ERASE all current data.\nContinue?",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;
            boolean success = rs.restoreFromFile(sqlFile);
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database restored successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refreshAllTables();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Restore failed. Check console output.",
                        "Restore Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void updateDashboardBanner() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof DashboardFrame) {
            ((DashboardFrame) window).updateMaintenanceBanner();
        }
    }
    private void deleteSectionAction() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = sectionsTable.convertRowIndexToModel(selectedRow);
        int sectionId = (Integer) sectionsTableModel.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Section ID: " + sectionId + "?\nThis will only work if the section has 0 enrolled students.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            String responseMessage = adminService.deleteSectionIfEmpty(sectionId);
            if (responseMessage.startsWith("SUCCESS:")) {
                JOptionPane.showMessageDialog(this, responseMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllTables();
            } else if (responseMessage.startsWith("WARNING:")) {
                JOptionPane.showMessageDialog(this, responseMessage, "Deletion Denied", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, responseMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public void refreshAllTables() {
        usersTableModel.setRowCount(0);
        studentsTableModel.setRowCount(0);
        instructorsTableModel.setRowCount(0);
        coursesTableModel.setRowCount(0);
        sectionsTableModel.setRowCount(0);
        List<UserAuth> users = adminService.getAllAuthUsers();
        for (UserAuth u : users) {
            usersTableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(), u.getStatus()});
        }
        List<Student> students = adminService.getAllStudents();
        for (Student s : students) {
            studentsTableModel.addRow(new Object[]{s.getUserId(), s.getRollNo(), s.getProgram(), s.getYear()});
        }
        List<Instructor> instructors = adminService.getAllInstructors();
        for (Instructor i : instructors) {
            instructorsTableModel.addRow(new Object[]{i.getInstructorId(), i.getUserId(), i.getName(), i.getEmail(), i.getDepartment()});
        }
        List<Course> courses = adminService.getAllCourses();
        for (Course c : courses) {
            coursesTableModel.addRow(new Object[]{c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits()});
        }
        List<Section> sections = adminService.getAllSections();
        for (Section s : sections) {
            sectionsTableModel.addRow(new Object[]{s.getSectionId(), s.getCourseId(), s.getInstructorId(), s.getDayTime(), s.getRoom(), s.getCapacity()});
        }
    }
}