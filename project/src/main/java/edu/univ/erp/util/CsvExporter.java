package edu.univ.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import edu.univ.erp.data.DbPool;

import java.io.BufferedWriter;
import java.io.File;
import java.sql.*;
import java.util.*;

public class CsvExporter {
    public static boolean exportToCsv(String[] headers, List<String[]> rows, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append(String.join(",", headers)).append("\n");
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    writer.append(escapeCsv(row[i]));
                    if (i < row.length - 1) writer.append(",");
                }
                writer.append("\n");
            }
            writer.flush();
            System.out.println("CSV exported: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
            return false;
        }
    }
    public static void exportGradeRoster(int sectionId, File outFile) throws Exception {
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            conn.setAutoCommit(true);
            String compSql = "SELECT component, weightage FROM section_weightage WHERE section_id = ? ORDER BY component";
            LinkedHashMap<String, Double> componentWeight = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(compSql)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String comp = rs.getString("component");
                        double w = rs.getDouble("weightage");
                        componentWeight.put(comp, w);
                    }
                }
            }
            String enrollSql = "SELECT e.enrollment_id, s.user_id, s.roll_no, u.username AS student_name " +
                    "FROM enrollments e " +
                    "JOIN students s ON e.student_id = s.user_id " +
                    "JOIN auth_db.users u ON s.user_id = u.user_id " +
                    "WHERE e.section_id = ? AND e.status = 'ENROLLED' " +
                    "ORDER BY s.roll_no";
            List<Map<String, Object>> rows = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(enrollSql)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("enrollment_id", rs.getInt("enrollment_id"));
                        m.put("user_id", rs.getInt("user_id"));
                        m.put("roll_no", rs.getString("roll_no"));
                        m.put("student_name", rs.getString("student_name"));
                        rows.add(m);
                    }
                }
            }
            String gradeSql = "SELECT enrollment_id, component, score FROM grades WHERE enrollment_id = ?";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                List<String> header = new ArrayList<>();
                header.add("Roll No");
                header.add("Student Name");
                for (String comp : componentWeight.keySet()) header.add(comp);
                header.add("Final Score");
                header.add("Grade");
                writer.write(String.join(",", header));
                writer.newLine();
                try (PreparedStatement psGrade = conn.prepareStatement(gradeSql)) {
                    for (Map<String, Object> enr : rows) {
                        int enrollmentId = (Integer) enr.get("enrollment_id");
                        String roll = (String) enr.get("roll_no");
                        String name = (String) enr.get("student_name");
                        Map<String, Double> scores = new HashMap<>();
                        psGrade.setInt(1, enrollmentId);
                        try (ResultSet rs = psGrade.executeQuery()) {
                            while (rs.next()) {
                                String comp = rs.getString("component");
                                double score = rs.getDouble("score");
                                scores.put(comp, score);
                            }
                        }
                        double finalScore = 0.0;
                        for (Map.Entry<String, Double> e : componentWeight.entrySet()) {
                            String comp = e.getKey();
                            double weight = e.getValue();
                            Double score = scores.get(comp);
                            if (score != null) {
                                finalScore += score * (weight / 100.0);
                            }
                        }
                        String letter = GradeUtils.scoreToLetter(finalScore);
                        List<String> fields = new ArrayList<>();
                        fields.add(CsvUtil.escape(roll));
                        fields.add(CsvUtil.escape(name));
                        for (String comp : componentWeight.keySet()) {
                            Double sc = scores.get(comp);
                            fields.add(CsvUtil.escape(sc == null ? "" : String.format("%.2f", sc)));
                        }
                        fields.add(CsvUtil.escape(String.format("%.2f", finalScore)));
                        fields.add(CsvUtil.escape(letter));
                        writer.write(String.join(",", fields));
                        writer.newLine();
                    }
                }
            }
        }
    }

    /**
     * Escapes special characters for valid CSV format.
     */
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
