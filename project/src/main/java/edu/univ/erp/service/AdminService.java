package edu.univ.erp.service;

import edu.univ.erp.data.AuthDao;
import edu.univ.erp.data.ErpDao;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.PasswordUtil;
import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final AuthDao authDao = new AuthDao();
    private final ErpDao erpDao = new ErpDao();
    public List<Course> getAllCourses() {
        return erpDao.getAllCourses();
    }
    public List<Section> getAllSections() {
        return erpDao.getAllSections();
    }
    public boolean backupAllDatabases() {
        return new BackupService().backup();
    }
    public List<UserAuth> getAllAuthUsers() {
        return authDao.getAllUsers();
    }
    public List<Student> getAllStudents() {
        return erpDao.getAllStudents();
    }
    public List<Instructor> getAllInstructors() {
        return erpDao.getAllInstructors();
    }
    public void loadInitialSettings() {
        System.out.println("[Service] Loading initial settings...");
        Settings currentSettings = erpDao.getSettings();
        AccessChecker.loadInitialSettings(currentSettings);
        System.out.println("[Service] Maintenance mode loaded as: " + AccessChecker.isMaintenanceMode());
    }
    public Settings getSettings() {
        return erpDao.getSettings();
    }
    public boolean updateRegistrationDeadline(java.sql.Date deadline) {
        boolean success = erpDao.updateRegistrationDeadline(deadline);
        if (!success) {
            System.err.println("[AdminService] Failed to update registration deadline");
        }
        return success;
    }
    public String deleteSectionIfEmpty(int sectionId) {
        int enrolledCount = erpDao.getEnrollmentCountBySection(sectionId);
        if (enrolledCount == -1) {
            return "ERROR: A database error occurred while checking enrollments.";
        }
        if (enrolledCount > 0) {
            return "WARNING: Cannot delete section " + sectionId + ". It has " + enrolledCount + " enrolled students.";
        }
        boolean success = erpDao.deleteSection(sectionId);
        if (success) {
            return "SUCCESS: Section " + sectionId + " deleted successfully!";
        } else {
            return "ERROR: Section deletion failed due to an unexpected transactional error.";
        }
    }
    public boolean updateMaintenanceMode(boolean isEnabled) {
        boolean dbSuccess = erpDao.updateSettings(isEnabled);
        if (dbSuccess) {
            AccessChecker.setMaintenanceMode(isEnabled);
            System.out.println("[Service] Maintenance mode updated to: " + isEnabled);
        } else {
            System.err.println("[Service] Failed to update maintenance mode in DB.");
        }
        return dbSuccess;
    }
    public UserAuth createNewUser(String username, String plainPassword, String role) {
        String hashed = PasswordUtil.hashPassword(plainPassword);
        return authDao.createUser(username, hashed, role);
    }
    public boolean createStudentProfile(Student student) throws SQLException {
        return erpDao.createStudentProfile(student);
    }
    public boolean createInstructorProfile(Instructor instructor) {
        return erpDao.createInstructorProfile(instructor);
    }
    public boolean createNewCourse(Course course) {
        return erpDao.createCourse(course);
    }
    public boolean createNewSection(Section section) {
        return erpDao.createSection(section);
    }
}