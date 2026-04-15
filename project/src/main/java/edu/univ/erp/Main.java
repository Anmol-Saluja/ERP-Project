package edu.univ.erp;

import edu.univ.erp.data.DbPool;
import edu.univ.erp.UI.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        try {
            DbPool.getAuthDataSource();
            DbPool.getErpDataSource();
            System.out.println("Database connection pools initialized successfully.");
        } catch (Exception ex) {
            System.err.println("ERROR initializing database pools:");
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
