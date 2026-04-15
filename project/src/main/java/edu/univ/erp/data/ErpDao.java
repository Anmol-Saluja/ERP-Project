package edu.univ.erp.data;

import edu.univ.erp.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErpDao {
    public Student getStudentByUserId(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student();
                s.setUserId(rs.getInt("user_id"));
                s.setRollNo(rs.getString("roll_no"));
                s.setProgram(rs.getString("program"));
                s.setYear(rs.getInt("year"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_no";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Student s = new Student();
                s.setUserId(rs.getInt("user_id"));
                s.setRollNo(rs.getString("roll_no"));
                s.setProgram(rs.getString("program"));
                s.setYear(rs.getInt("year"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Instructor> getAllInstructors() {
        List<Instructor> list = new ArrayList<>();
        String sql = "SELECT * FROM instructors ORDER BY name";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Instructor i = new Instructor();
                i.setInstructorId(rs.getInt("instructor_id"));
                i.setUserId(rs.getInt("user_id"));
                i.setName(rs.getString("name"));
                i.setEmail(rs.getString("email"));
                i.setDepartment(rs.getString("department"));
                list.add(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public Instructor getInstructorByUserId(int userId) {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instructor i = new Instructor();
                    i.setInstructorId(rs.getInt("instructor_id"));
                    i.setUserId(rs.getInt("user_id"));
                    i.setName(rs.getString("name"));
                    i.setEmail(rs.getString("email"));
                    i.setDepartment(rs.getString("department"));
                    return i;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCode(rs.getString("code"));
                c.setTitle(rs.getString("title"));
                c.setCredits(rs.getInt("credits"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCode(rs.getString("code"));
                c.setTitle(rs.getString("title"));
                c.setCredits(rs.getInt("credits"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Section> getAllSections() {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections ORDER BY section_id";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));
                s.setCapacity(rs.getInt("capacity"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setDropDeadline(rs.getDate("drop_deadline"));
                s.setGradesReleased(rs.getBoolean("grades_released"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Section> getSectionsByInstructor(int instructorId) {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections WHERE instructor_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));
                s.setCapacity(rs.getInt("capacity"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setDropDeadline(rs.getDate("drop_deadline"));
                s.setGradesReleased(rs.getBoolean("grades_released"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public Section getSectionById(int sectionId) {
        String sql = "SELECT * FROM sections WHERE section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Section s = new Section();
                    s.setSectionId(rs.getInt("section_id"));
                    s.setCourseId(rs.getInt("course_id"));
                    s.setInstructorId(rs.getInt("instructor_id"));
                    s.setDayTime(rs.getString("day_time"));
                    s.setRoom(rs.getString("room"));
                    s.setCapacity(rs.getInt("capacity"));
                    s.setSemester(rs.getString("semester"));
                    s.setYear(rs.getInt("year"));
                    s.setDropDeadline(rs.getDate("drop_deadline"));
                    s.setGradesReleased(rs.getBoolean("grades_released"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Map<Integer, Map<String, Double>> getGradesBySection(int sectionId) {
        Map<Integer, Map<String, Double>> gradeMap = new HashMap<>();
        Map<Integer, Map<String, Integer>> weightMap = new HashMap<>();
        String sql = "SELECT g.enrollment_id, g.component, g.score, g.weightage " +
                "FROM grades g JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    String component = rs.getString("component");
                    double score = rs.getDouble("score");
                    int weightage = rs.getInt("weightage");
                    gradeMap.putIfAbsent(enrollmentId, new HashMap<>());
                    weightMap.putIfAbsent(enrollmentId, new HashMap<>());
                    if (!rs.wasNull()) {
                        gradeMap.get(enrollmentId).put(component, score);
                    }
                    weightMap.get(enrollmentId).put(component, weightage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gradeMap;
    }
    public List<String> getGradeComponentsBySection(int sectionId) {
        List<String> components = new ArrayList<>();
        String sql = "SELECT DISTINCT g.component FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY g.component";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    components.add(rs.getString("component"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return components;
    }
    public List<Double> getScoresBySectionAndComponent(int sectionId, String component) {
        List<Double> scores = new ArrayList<>();
        String sql = "SELECT g.score FROM grades g " +
                "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? AND g.component = ? AND g.score IS NOT NULL";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, component);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    scores.add(rs.getDouble("score"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }
    public void upsertGrade(int enrollmentId, String component, double score, int weightage) throws SQLException {
        String deleteSql = "DELETE FROM grades WHERE enrollment_id = ? AND component = ?";
        String insertSql = "INSERT INTO grades (enrollment_id, component, score, weightage) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                psDel.setInt(1, enrollmentId);
                psDel.setString(2, component);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                psIns.setInt(1, enrollmentId);
                psIns.setString(2, component);
                psIns.setDouble(3, score);
                psIns.setInt(4, weightage);
                psIns.executeUpdate();
            }
        }
    }
    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql =
                "SELECT DISTINCT e.enrollment_id, e.student_id, e.section_id, e.status, e.final_grade, " +
                        "       s.day_time, s.room, s.year, s.semester, s.grades_released, " +
                        "       c.code, c.title " +
                        "FROM enrollments e " +
                        "JOIN sections s ON e.section_id = s.section_id " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "WHERE e.student_id = ? " +
                        "ORDER BY s.year DESC, s.semester DESC";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Enrollment e = new Enrollment();
                e.setEnrollmentId(rs.getInt("enrollment_id"));
                e.setStudentId(rs.getInt("student_id"));
                e.setSectionId(rs.getInt("section_id"));
                e.setStatus(rs.getString("status"));
                e.setCourseCode(rs.getString("code"));
                e.setCourseTitle(rs.getString("title"));
                e.setDayTime(rs.getString("day_time"));
                e.setRoom(rs.getString("room"));
                e.setGradesReleased(rs.getBoolean("grades_released"));
                e.setFinalGrade(rs.getString("final_grade"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean updateFinalGrade(int enrollmentId, String letterGrade) {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, letterGrade);
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Grade> getGradesByEnrollmentId(int enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Grade g = new Grade();
                g.setGradeId(rs.getInt("grade_id"));
                g.setEnrollmentId(rs.getInt("enrollment_id"));
                g.setComponent(rs.getString("component"));
                g.setScore(rs.getDouble("score"));
                g.setWeightage(rs.getInt("weightage"));
                grades.add(g);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return grades;
    }
    public int getEnrollmentCountBySection(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public boolean deleteSection(int sectionId) {
        String sqlDeleteWeightage = "DELETE FROM section_weightage WHERE section_id = ?";
        String sqlDeleteSection = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psWeightage = conn.prepareStatement(sqlDeleteWeightage);
                 PreparedStatement psSection = conn.prepareStatement(sqlDeleteSection)) {
                psWeightage.setInt(1, sectionId);
                psWeightage.executeUpdate();
                psSection.setInt(1, sectionId);
                int rowsAffected = psSection.executeUpdate();
                if (rowsAffected == 1) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
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
    public List<Enrollment> getEnrollmentsBySection(int sectionId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.section_id, e.status, e.student_id,
                   st.roll_no, u.username AS student_name
            FROM enrollments e
            JOIN students st ON e.student_id = st.user_id
            JOIN auth_db.users u ON st.user_id = u.user_id
            WHERE e.section_id = ?
        """;
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Enrollment e = new Enrollment();
                e.setEnrollmentId(rs.getInt("enrollment_id"));
                e.setStudentId(rs.getInt("student_id"));
                e.setSectionId(rs.getInt("section_id"));
                e.setStatus(rs.getString("status"));
                e.setStudentName(rs.getString("student_name"));
                e.setRollNo(rs.getString("roll_no"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean enrollStudent(int studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'ENROLLED')";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw e;
        }
    }
    public boolean dropEnrollment(int enrollmentId) throws SQLException {
        String sqlGrades = "DELETE FROM grades WHERE enrollment_id = ?";
        String sqlEnroll = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psGrades = conn.prepareStatement(sqlGrades)) {
                psGrades.setInt(1, enrollmentId);
                psGrades.executeUpdate();
            }
            int rowsAffected;
            try (PreparedStatement psEnroll = conn.prepareStatement(sqlEnroll)) {
                psEnroll.setInt(1, enrollmentId);
                rowsAffected = psEnroll.executeUpdate();
            }
            conn.commit();
            return rowsAffected == 1;
        } catch (SQLException e) {
            throw e;
        }
    }
    public Map<String, Integer> getWeightageBySection(int sectionId) {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT component, weightage FROM section_weightage WHERE section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("component"), rs.getInt("weightage"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    public boolean upsertSectionWeightage(int sectionId, String component, int weightage) {
        String sql =
                "INSERT INTO section_weightage (section_id, component, weightage) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE weightage = VALUES(weightage)";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, component);
            ps.setInt(3, weightage);
            return ps.executeUpdate() >= 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateRegistrationDeadline(java.sql.Date deadline) {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'course_registration_deadline'";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deadline.toString());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating course registration deadline.");
            return false;
        }
    }
    public Settings getSettings() {
        Settings settings = new Settings();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("setting_key");
                String value = rs.getString("setting_value");
                if ("maintenance_mode".equalsIgnoreCase(key)) {
                    boolean isEnabled = Boolean.parseBoolean(value);
                    settings.setMaintenanceMode(isEnabled);
                }
                if ("course_registration_deadline".equalsIgnoreCase(key)) {
                    if (value != null && value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        settings.setCourseRegistrationDeadline(
                                java.sql.Date.valueOf(value)
                        );
                    } else {
                        System.err.println("Invalid deadline format in Database: " + value);
                        settings.setCourseRegistrationDeadline(null);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching settings.");
        }
        return settings;
    }
    public boolean updateSettings(boolean isEnabled) {
        String sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_mode'";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(isEnabled));
            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating settings.");
            return false;
        }
    }
    public boolean createStudentProfile(Student student) throws SQLException {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, student.getUserId());
            ps.setString(2, student.getRollNo());
            ps.setString(3, student.getProgram());
            ps.setInt(4, student.getYear());
            return ps.executeUpdate() == 1;
        }
    }
    public boolean createInstructorProfile(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, name, email, department) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructor.getUserId());
            ps.setString(2, instructor.getName());
            ps.setString(3, instructor.getEmail());
            ps.setString(4, instructor.getDepartment());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean createCourse(Course course) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getTitle());
            ps.setInt(3, course.getCredits());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isGradesReleasedByEnrollmentId(int enrollmentId) {
        String sql = "SELECT s.grades_released FROM sections s " +
                "JOIN enrollments e ON s.section_id = e.section_id " +
                "WHERE e.enrollment_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("grades_released");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateGradeReleaseStatus(int sectionId, boolean released) {
        String sql = "UPDATE sections SET grades_released = ? WHERE section_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, released);
            ps.setInt(2, sectionId);
            int updated = ps.executeUpdate();
            if (updated != 1) return false;
            if (released) {
                String q = "SELECT st.user_id FROM enrollments e JOIN students st ON e.student_id = st.user_id WHERE e.section_id = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(q)) {
                    ps2.setInt(1, sectionId);
                    try (ResultSet rs = ps2.executeQuery()) {
                        NotificationDao nd = new NotificationDao();
                        while (rs.next()) {
                            int studentUserId = rs.getInt("user_id");
                            nd.createNotification(studentUserId, "Grades released for section " + sectionId + ". Check your Academic Record.");
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getFinalGrade(int enrollmentId) {
        String sql = "SELECT final_grade FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("final_grade");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean createSection(Section section) {
        if (section.getCapacity() < 0) {
            System.err.println("Rejected: Negative capacity not allowed.");
            return false;
        }
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbPool.getErpDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, section.getCourseId());
            ps.setInt(2, section.getInstructorId());
            ps.setString(3, section.getDayTime());
            ps.setString(4, section.getRoom());
            ps.setInt(5, section.getCapacity());
            ps.setString(6, section.getSemester());
            ps.setInt(7, section.getYear());
            int rows = ps.executeUpdate();
            if (rows != 1) return false;
            int newSectionId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) newSectionId = keys.getInt(1);
            }
            String message = "New section created (Section " + (newSectionId == -1 ? "?" : newSectionId) +
                    ") for Course ID " + section.getCourseId();
            try (PreparedStatement q = conn.prepareStatement("SELECT user_id FROM instructors WHERE instructor_id = ?")) {
                q.setInt(1, section.getInstructorId());
                try (ResultSet r = q.executeQuery()) {
                    if (r.next()) {
                        int instrUserId = r.getInt("user_id");
                        NotificationDao nd = new NotificationDao();
                        nd.createNotification(instrUserId, "You were assigned a new section: " + message);
                    }
                }
            }
            try (PreparedStatement q = conn.prepareStatement("SELECT user_id FROM auth_db.users WHERE role = 'STUDENT' AND status = 'ACTIVE'")) {
                try (ResultSet r = q.executeQuery()) {
                    NotificationDao nd = new NotificationDao();
                    while (r.next()) {
                        int studentUserId = r.getInt("user_id");
                        nd.createNotification(studentUserId, "New section opened: " + message);
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}