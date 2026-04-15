package edu.univ.erp.domain;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;
    public Notification() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { isRead = read; }
}
