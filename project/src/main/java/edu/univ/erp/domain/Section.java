package edu.univ.erp.domain;

import java.sql.Date;

public class Section {
    private int sectionId;
    private int courseId;
    private int instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;
    private Date dropDeadline;
    private boolean gradesReleased;
    private String courseCode;
    public Section() {}
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public void setGradesReleased(boolean gradesReleased) {
        this.gradesReleased = gradesReleased;
    }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }
    public String getDayTime() { return dayTime; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public void setDropDeadline(Date dropDeadline) { this.dropDeadline = dropDeadline; }
    public String getCourseCode() { return courseCode; }
}
