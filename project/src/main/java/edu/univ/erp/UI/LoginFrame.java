package edu.univ.erp.UI;

import edu.univ.erp.service.*;
import edu.univ.erp.domain.UserAuth;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private final AuthService authService = new AuthService();
    private int failedAttempts = 0;
    private long lockoutEndTime = 0;
    private Timer lockoutTimer;
    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);
        loginButton = new JButton("Login");
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(formPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        loginButton.addActionListener(e -> handleLogin());
    }
    private void handleLogin() {
        if (System.currentTimeMillis() < lockoutEndTime) {
            showLockoutRemaining();
            return;
        }
        if (lockoutTimer != null) lockoutTimer.stop();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        UserAuth user = authService.authenticate(username, password);
        if (user != null) {
            failedAttempts = 0;
            lockoutEndTime = 0;
            statusLabel.setText("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            JOptionPane.showMessageDialog(
                    this,
                    "Welcome " + user.getUsername() + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            openDashboard(user);
        } else {
            failedAttempts++;
            statusLabel.setText("Invalid credentials (" + failedAttempts + "/5)");
            if (failedAttempts >= 5) {
                startLockout();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void startLockout() {
        failedAttempts = 5;
        lockoutEndTime = System.currentTimeMillis() + (5 * 60 * 1000);
        loginButton.setEnabled(false);
        JOptionPane.showMessageDialog(
                this,
                "Too many failed attempts.\nPlease wait 5 minutes before trying again.",
                "Account Locked",
                JOptionPane.ERROR_MESSAGE
        );
        lockoutTimer = new Timer(1000, e -> showLockoutRemaining());
        lockoutTimer.start();
    }
    private void showLockoutRemaining() {
        long remaining = lockoutEndTime - System.currentTimeMillis();
        if (remaining <= 0) {
            statusLabel.setText("You may try again.");
            loginButton.setEnabled(true);
            failedAttempts = 0;
            lockoutTimer.stop();
            return;
        }
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long sec = seconds % 60;
        statusLabel.setText(
                String.format("Locked out. Try again in %02d:%02d", minutes, sec)
        );
    }
    private void openDashboard(UserAuth user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame(user);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
    public static void main(String[] args) {
        System.out.println("Application starting up...");
        AdminService adminService = new AdminService();
        adminService.loadInitialSettings();
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
