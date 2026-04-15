package edu.univ.erp.UI;

import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.AccessChecker;
import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final UserAuth user;
    private final JLabel maintenanceBanner;
    private JMenuBar menuBar;
    private JMenu accountMenu;
    private JMenuItem logoutItem;
    public DashboardFrame(UserAuth user) {
        this.user = user;
        setTitle("University ERP - Dashboard (" + user.getRole() + ")");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setupMenuBar();
        setJMenuBar(menuBar);
        JLabel header = new JLabel(
                "Welcome, " + user.getUsername() + " [" + user.getRole() + "]",
                SwingConstants.CENTER
        );
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);
        switch (user.getRole().toUpperCase()) {
            case "STUDENT" -> add(new StudentPanel(user), BorderLayout.CENTER);
            case "INSTRUCTOR" -> add(new InstructorPanel(user), BorderLayout.CENTER);
            case "ADMIN" -> add(new AdminPanel(user), BorderLayout.CENTER);
            default -> add(new JLabel("Unknown role: " + user.getRole(), SwingConstants.CENTER));
        }
        maintenanceBanner = new JLabel("Application is in MAINTENANCE MODE. All changes are disabled.", SwingConstants.CENTER);
        maintenanceBanner.setFont(new Font("SansSerif", Font.BOLD, 14));
        maintenanceBanner.setForeground(Color.WHITE);
        maintenanceBanner.setBackground(Color.RED);
        maintenanceBanner.setOpaque(true);
        add(maintenanceBanner, BorderLayout.SOUTH);
        updateMaintenanceBanner();
    }
    public void updateMaintenanceBanner() {
        if (maintenanceBanner != null) {
            maintenanceBanner.setVisible(AccessChecker.isMaintenanceMode());
            revalidate();
            repaint();
        }
    }
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        accountMenu = new JMenu("Account");
        JMenuItem changePassItem = new JMenuItem("Change Password");
        changePassItem.addActionListener(e -> {
            String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
            if (newPass != null && !newPass.trim().isEmpty()) {
                edu.univ.erp.service.AuthService authService = new edu.univ.erp.service.AuthService();
                if (authService.changePassword(user.getUsername(), newPass)) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error changing password.");
                }
            }
        });
        logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(changePassItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);

        menuBar.add(accountMenu);
    }
    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}