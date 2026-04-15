package edu.univ.erp.service;

import edu.univ.erp.data.ErpDao;
import edu.univ.erp.domain.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentService {
    private final ErpDao erpDao = new ErpDao();
    private Map<Integer, Course> courseCache;
    private Map<Integer, Instructor> instructorCache;
    public enum EnrollmentResponse {
        SUCCESS("Enrolled successfully!"),
        DROPPED("Dropped successfully!"),
        MAINTENANCE_MODE("Changes cannot be made while in maintenance mode."),
        SECTION_FULL("This section is full."),
        ALREADY_ENROLLED("You are already enrolled in this section."),
        DATABASE_ERROR("An unexpected database error occurred."),
        FAILED("The operation failed.");
        public final String message;
        EnrollmentResponse(String message) { this.message = message; }
    }
    public StudentService() {
        try {
            this.courseCache = erpDao.getAllCourses().stream()
                    .collect(Collectors.toMap(Course::getCourseId, c -> c));
            this.instructorCache = erpDao.getAllInstructors().stream()
                    .collect(Collectors.toMap(Instructor::getInstructorId, i -> i));
        } catch (Exception e) {
            System.err.println("Error pre-caching courses/instructors: " + e.getMessage());
            this.courseCache = Collections.emptyMap();
            this.instructorCache = Collections.emptyMap();
        }
    }
    public Student getStudentProfile(int userId) {
        return erpDao.getStudentByUserId(userId);
    }
    public List<Section> getAllAvailableSections() {
        return erpDao.getAllSections();
    }
    public Section getSectionById(int sectionId) {
        return erpDao.getSectionById(sectionId);
    }
    public Course getCourseById(int courseId) {
        return courseCache.get(courseId);
    }
    public Instructor getInstructorByInstructorId(int instructorId) {
        return instructorCache.get(instructorId);
    }
    public List<Enrollment> getDetailedEnrollmentsByStudent(int studentId) {
        List<Enrollment> enrollments = erpDao.getEnrollmentsByStudent(studentId);
        final String notReleased = "---";
        for (Enrollment enrollment : enrollments) {
            if (!enrollment.isGradesReleased()) {
                enrollment.setFinalGrade(notReleased);
            }
        }
        return enrollments;
    }
    public List<Grade> getGradesByEnrollmentId(int enrollmentId) {
        if (!erpDao.isGradesReleasedByEnrollmentId(enrollmentId)) {
            return Collections.emptyList();
        }
        return erpDao.getGradesByEnrollmentId(enrollmentId);
    }
    public EnrollmentResponse enrollInSection(int studentId, int sectionId) {
        if (AccessChecker.isMaintenanceMode()) {
            return EnrollmentResponse.MAINTENANCE_MODE;
        }
        Section section = erpDao.getSectionById(sectionId);
        if (section == null) {
            return EnrollmentResponse.FAILED;
        }
        int currentEnrollment = erpDao.getEnrollmentCountBySection(sectionId);
        if (currentEnrollment >= section.getCapacity()) {
            return EnrollmentResponse.SECTION_FULL;
        }
        try {
            if (erpDao.enrollStudent(studentId, sectionId)) {
                return EnrollmentResponse.SUCCESS;
            } else {
                return EnrollmentResponse.FAILED;
            }
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                return EnrollmentResponse.ALREADY_ENROLLED;
            }
            e.printStackTrace();
            return EnrollmentResponse.DATABASE_ERROR;
        }
    }
    public EnrollmentResponse dropEnrollment(int enrollmentId) {
        if (AccessChecker.isMaintenanceMode()) {
            return EnrollmentResponse.MAINTENANCE_MODE;
        }
        try {
            if (erpDao.dropEnrollment(enrollmentId)) {
                return EnrollmentResponse.DROPPED;
            } else {
                return EnrollmentResponse.FAILED;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return EnrollmentResponse.DATABASE_ERROR;
        }
    }
    public Settings getSettings() {
        return erpDao.getSettings();
    }
}

