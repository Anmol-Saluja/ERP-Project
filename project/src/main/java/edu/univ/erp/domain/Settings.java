package edu.univ.erp.domain;

public class Settings {
    private boolean maintenanceMode;
    private java.sql.Date courseRegistrationDeadline;
    public Settings() {
        this.maintenanceMode = false;
        this.courseRegistrationDeadline = null;
    }
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
    public java.sql.Date getCourseRegistrationDeadline() {
        return courseRegistrationDeadline;
    }
    public void setCourseRegistrationDeadline(java.sql.Date courseRegistrationDeadline) {
        this.courseRegistrationDeadline = courseRegistrationDeadline;
    }
}
