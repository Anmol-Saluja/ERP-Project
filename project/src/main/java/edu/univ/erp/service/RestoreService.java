package edu.univ.erp.service;

import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class RestoreService {
    private static String MYSQL_USER;
    private static String MYSQL_PASSWORD;
    static {
        try (InputStream input = RestoreService.class.getClassLoader().getResourceAsStream("app.properties")) {
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
    private String findMysql() {
        String[] paths = {
                "/opt/homebrew/bin/mysql",
                "/usr/local/bin/mysql",
                "/usr/bin/mysql",
                "/usr/local/mysql/bin/mysql"
        };
        for (String p : paths) {
            if (new File(p).exists()) {
                System.out.println("✔ Using mysql: " + p);
                return p;
            }
        }
        JOptionPane.showMessageDialog(null,
                "mysql client not found.\nInstall MySQL client tools.",
                "Restore Error",
                JOptionPane.ERROR_MESSAGE);
        return null;
    }
    public boolean restoreFromFile(File sqlFile) {
        try {
            if (!sqlFile.exists()) {
                JOptionPane.showMessageDialog(null,
                        "Backup SQL file does not exist!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String mysqlPath = findMysql();
            if (mysqlPath == null) return false;
            ProcessBuilder pb = new ProcessBuilder(
                    mysqlPath,
                    "-u", MYSQL_USER,
                    "-p" + MYSQL_PASSWORD,
                    "--force"
            );
            pb.redirectInput(sqlFile);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[MYSQL] " + line);
            }
            int exitCode = process.waitFor();
            System.out.println("Restore exit code = " + exitCode);
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Restore failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
