package edu.univ.erp.service;

import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.io.InputStream;

public class BackupService {
    private static String MYSQL_USER;
    private static String MYSQL_PASSWORD;
    static {
        try (InputStream input = BackupService.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties props = new Properties();
            props.load(input);
            MYSQL_USER = props.getProperty("erp.username");
            MYSQL_PASSWORD = props.getProperty("erp.password");
        } catch (Exception e) {
            e.printStackTrace();
            MYSQL_USER = "root";
            MYSQL_PASSWORD = "";
        }
    }
    private static final String[] DATABASES = {"erp_db", "auth_db"};
    private String findMySqlDump() {
        String[] paths = {
                "/opt/homebrew/bin/mysqldump",
                "/usr/local/bin/mysqldump",
                "/usr/bin/mysqldump",
                "/usr/local/mysql/bin/mysqldump"
        };
        for (String p : paths) {
            if (new File(p).exists()) {
                System.out.println("✔ Using mysqldump: " + p);
                return p;
            }
        }
        JOptionPane.showMessageDialog(null,
                "mysqldump not found on your system.\nInstall MySQL client tools.",
                "Backup Error",
                JOptionPane.ERROR_MESSAGE);
        return null;
    }
    public boolean backup() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select folder to save backup");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
                return false;
            File dir = chooser.getSelectedFile();
            if (!dir.exists()) {
                JOptionPane.showMessageDialog(null, "Selected folder does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String dumpPath = findMySqlDump();
            if (dumpPath == null) return false;
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outFile = new File(dir, "backup_erp_auth_" + timestamp + ".sql");
            ProcessBuilder pb = new ProcessBuilder(
                    dumpPath,
                    "-u", MYSQL_USER,
                    "-p" + MYSQL_PASSWORD,
                    "--databases", DATABASES[0], DATABASES[1]
            );
            pb.redirectOutput(outFile);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                JOptionPane.showMessageDialog(null,
                        "Backup failed! Exit code: " + exit,
                        "Backup Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            JOptionPane.showMessageDialog(null,
                    "Backup created successfully:\n" + outFile.getAbsolutePath(),
                    "Backup Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Backup failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
