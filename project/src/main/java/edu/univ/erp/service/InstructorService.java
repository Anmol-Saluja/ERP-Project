package edu.univ.erp.service;

import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class InstructorService {
    private final ErpDao erpDao = new ErpDao();
    public Map<String, Integer> getSectionWeightage(int sectionId) {
        return erpDao.getWeightageBySection(sectionId);
    }
    public Instructor getInstructorProfile(int userId) {
        return erpDao.getInstructorByUserId(userId);
    }
    public List<Section> getSectionsByInstructor(int instructorId) {
        return erpDao.getSectionsByInstructor(instructorId);
    }
    public File exportGradeRosterForSection(int sectionId, String outPath) throws Exception {
        File out = new File(outPath);
        CsvExporter.exportGradeRoster(sectionId, out);
        return out;
    }
    public ImportResult importGradeRosterFromFile(int sectionId, File csvFile) throws Exception {
        return CsvImporter.importGradeRoster(sectionId, csvFile);
    }
    public boolean saveSectionWeightage(int sectionId, Map<String,Integer> weightMap) {
        if (!AccessChecker.canMakeChanges(null)) {
            System.err.println("Weightage save/update blocked: Maintenance Mode is ON.");
            return false;
        }
        for (var entry : weightMap.entrySet()) {
            erpDao.upsertSectionWeightage(sectionId, entry.getKey(), entry.getValue());
        }
        return true;
    }
    public boolean updateFinalGrade(int enrollmentId, String finalGrade) {
        return erpDao.updateFinalGrade(enrollmentId, finalGrade);
    }
    public List<Enrollment> getEnrollmentsBySection(int sectionId) {
        return erpDao.getEnrollmentsBySection(sectionId);
    }
    public Map<Integer, Map<String, Double>> getGradesForSection(int sectionId) {
        return erpDao.getGradesBySection(sectionId);
    }
    public boolean releaseGrades(int sectionId) {
        if (!AccessChecker.canMakeChanges(null)) {
            System.err.println("Grade release blocked: Maintenance Mode is ON.");
            return false;
        }
        return erpDao.updateGradeReleaseStatus(sectionId, true);
    }
    public List<String> getGradeComponentsForSection(int sectionId) {
        List<String> components = erpDao.getGradeComponentsBySection(sectionId);
        return components.stream().distinct().collect(Collectors.toList());
    }
    public String computeFinalGrade(Map<String, Double> scoreMap, Map<String, Integer> weightMap) {
        if (scoreMap == null || scoreMap.isEmpty()) return "-";
        if (weightMap == null || weightMap.isEmpty()) return "-";
        double totalWeighted = 0;
        int totalWeights = 0;
        for (String comp : scoreMap.keySet()) {
            double score = scoreMap.getOrDefault(comp, 0.0);
            int weight = weightMap.getOrDefault(comp, 0);
            totalWeighted += score * weight;
            totalWeights += weight;
        }
        if (totalWeights == 0) return "-";
        double finalScore = totalWeighted / totalWeights;
        return GradeUtils.scoreToLetter(finalScore);
    }
    public Map<String, Double> calculateComponentStatistics(int sectionId, String component) {
        List<Double> scores = erpDao.getScoresBySectionAndComponent(sectionId, component);
        Map<String, Double> stats = new HashMap<>();
        if (scores.isEmpty()) {
            return stats;
        }
        stats.put("Min", scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        stats.put("Max", scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        stats.put("Avg", scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        Collections.sort(scores);
        double median;
        int size = scores.size();
        if (size % 2 == 1) {
            median = scores.get(size / 2);
        } else {
            median = (scores.get(size / 2 - 1) + scores.get(size / 2)) / 2.0;
        }
        stats.put("Median", median);
        return stats;
    }
    public boolean saveGrades(List<Grade> gradesToSave) {
        if (!AccessChecker.canMakeChanges(null)) {
            System.err.println("Grade save blocked: Maintenance Mode is ON.");
            return false;
        }
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Grade grade : gradesToSave) {
                    erpDao.upsertGrade(
                            grade.getEnrollmentId(),
                            grade.getComponent(),
                            grade.getScore(),
                            grade.getWeightage()
                    );
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}