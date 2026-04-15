package edu.univ.erp.util;

import edu.univ.erp.data.DbPool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

public class CsvImporter {
    public static ImportResult importGradeRoster(int sectionId, File csvFile) throws Exception {
        int changed = 0;
        Set<String> missingRolls = new LinkedHashSet<>();
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String headerLine = br.readLine();
                if (headerLine == null) throw new IllegalArgumentException("Empty CSV");
                List<String> headers = CsvUtil.parseLine(headerLine);
                int idxRoll = -1, idxName = -1, idxFinalScore = -1, idxLetter = -1;
                List<Integer> componentIdx = new ArrayList<>();
                List<String> componentNames = new ArrayList<>();
                for (int i = 0; i < headers.size(); i++) {
                    String h = headers.get(i).trim();
                    String low = h.toLowerCase();
                    if (low.equals("roll no") || low.equals("roll_no") || low.equals("roll")) idxRoll = i;
                    else if (low.equals("student name") || low.equals("name")) idxName = i;
                    else if (low.equals("final score") || low.equals("final_score")) idxFinalScore = i;
                    else if (low.equals("grade") || low.equals("letter")) idxLetter = i;
                    else {
                        componentIdx.add(i);
                        componentNames.add(h);
                    }
                }
                if (idxRoll == -1) throw new IllegalArgumentException("CSV must contain 'Roll No' column");
                String findStudent = "SELECT user_id FROM students WHERE roll_no = ?";
                String findEnroll = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
                String findGrade = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
                String insertGrade = "INSERT INTO grades (enrollment_id, component, score, weightage) VALUES (?, ?, ?, ?)";
                String updateGrade = "UPDATE grades SET score = ? WHERE grade_id = ?";
                String getWeight = "SELECT weightage FROM section_weightage WHERE section_id = ? AND component = ?";
                try (PreparedStatement psFindStudent = conn.prepareStatement(findStudent);
                     PreparedStatement psFindEnroll = conn.prepareStatement(findEnroll);
                     PreparedStatement psFindGrade = conn.prepareStatement(findGrade);
                     PreparedStatement psInsertGrade = conn.prepareStatement(insertGrade);
                     PreparedStatement psUpdateGrade = conn.prepareStatement(updateGrade);
                     PreparedStatement psGetWeight = conn.prepareStatement(getWeight)) {
                    String line;
                    int lineNumber = 1;
                    while ((line = br.readLine()) != null) {
                        lineNumber++;
                        if (line.trim().isEmpty()) continue;
                        List<String> cols = CsvUtil.parseLine(line);
                        String roll = idxRoll < cols.size() ? cols.get(idxRoll) : null;
                        if (roll == null || roll.trim().isEmpty()) continue;
                        roll = roll.trim();
                        Integer studentId = null;
                        psFindStudent.setString(1, roll);
                        try (ResultSet rs = psFindStudent.executeQuery()) {
                            if (rs.next()) studentId = rs.getInt("user_id");
                        }
                        if (studentId == null) {
                            missingRolls.add(roll);
                            continue;
                        }
                        Integer enrollmentId = null;
                        psFindEnroll.setInt(1, studentId);
                        psFindEnroll.setInt(2, sectionId);
                        try (ResultSet rs = psFindEnroll.executeQuery()) {
                            if (rs.next()) enrollmentId = rs.getInt("enrollment_id");
                        }
                        if (enrollmentId == null) {
                            continue;
                        }
                        Map<String, Double> newScores = new HashMap<>();
                        for (int k = 0; k < componentIdx.size(); k++) {
                            int ci = componentIdx.get(k);
                            String comp = componentNames.get(k);
                            String sval = ci < cols.size() ? cols.get(ci) : null;
                            if (sval == null || sval.trim().isEmpty()) continue;
                            double score;
                            try { score = Double.parseDouble(sval.trim()); } catch (NumberFormatException ex) { continue; }
                            newScores.put(comp, score);
                            psFindGrade.setInt(1, enrollmentId);
                            psFindGrade.setString(2, comp);
                            Integer gradeId = null;
                            try (ResultSet grd = psFindGrade.executeQuery()) {
                                if (grd.next()) gradeId = grd.getInt("grade_id");
                            }
                            if (gradeId == null) {
                                double weight = 0.0;
                                psGetWeight.setInt(1, sectionId);
                                psGetWeight.setString(2, comp);
                                try (ResultSet wrs = psGetWeight.executeQuery()) {
                                    if (wrs.next()) weight = wrs.getDouble("weightage");
                                }
                                psInsertGrade.setInt(1, enrollmentId);
                                psInsertGrade.setString(2, comp);
                                psInsertGrade.setDouble(3, score);
                                psInsertGrade.setDouble(4, weight);
                                changed += psInsertGrade.executeUpdate();
                            } else {
                                psUpdateGrade.setDouble(1, score);
                                psUpdateGrade.setInt(2, gradeId);
                                changed += psUpdateGrade.executeUpdate();
                            }
                        }
                        double finalScore = 0.0;
                        String weightSql = "SELECT component, weightage FROM section_weightage WHERE section_id = ?";
                        try (PreparedStatement psW = conn.prepareStatement(weightSql)) {
                            psW.setInt(1, sectionId);
                            try (ResultSet wrs = psW.executeQuery()) {
                                while (wrs.next()) {
                                    String comp = wrs.getString("component");
                                    double w = wrs.getDouble("weightage");
                                    Double s = newScores.get(comp);
                                    if (s != null) finalScore += s * (w / 100.0);
                                    else {
                                        String readScore = "SELECT score FROM grades WHERE enrollment_id = ? AND component = ?";
                                        try (PreparedStatement psRead = conn.prepareStatement(readScore)) {
                                            psRead.setInt(1, enrollmentId);
                                            psRead.setString(2, comp);
                                            try (ResultSet r2 = psRead.executeQuery()) {
                                                if (r2.next()) {
                                                    finalScore += r2.getDouble("score") * (w / 100.0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        String letter = GradeUtils.scoreToLetter(finalScore);
                        String updateEnroll = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
                        try (PreparedStatement psU = conn.prepareStatement(updateEnroll)) {
                            psU.setString(1, letter);
                            psU.setInt(2, enrollmentId);
                            psU.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        }
        return new ImportResult(changed, missingRolls);
    }
}
