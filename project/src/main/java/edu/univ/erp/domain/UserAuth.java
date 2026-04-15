package edu.univ.erp.domain;

public class UserAuth {
    private int userId;
    private String username;
    private String role;
    private String status;
    private String passwordHash;
    public UserAuth() {}
    public UserAuth(int userId, String username, String role, String status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isInstructor() { return "INSTRUCTOR".equalsIgnoreCase(role); }
    public boolean isStudent() { return "STUDENT".equalsIgnoreCase(role); }
}
