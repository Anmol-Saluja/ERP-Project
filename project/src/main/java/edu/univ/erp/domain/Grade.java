package edu.univ.erp.domain;

public class Grade {
    private int gradeId;
    private int enrollmentId;
    private String component;
    private double score;
    private int weightage;
    public Grade() {}
    public int getWeightage() {
        return weightage;
    }
    public void setWeightage(int weightage) {
        this.weightage = weightage;
    }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
