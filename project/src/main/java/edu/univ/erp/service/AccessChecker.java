package edu.univ.erp.service;

import edu.univ.erp.domain.Settings;
import edu.univ.erp.domain.UserAuth;
import java.util.concurrent.atomic.AtomicBoolean; // Added

public class AccessChecker {
    private static final AtomicBoolean maintenanceMode = new AtomicBoolean(false);
    public static void loadInitialSettings(Settings settings) {
        if (settings != null) {
            maintenanceMode.set(settings.isMaintenanceMode());
        }
    }
    public static void setMaintenanceMode(boolean isEnabled) {
        maintenanceMode.set(isEnabled);
    }
    public static boolean isMaintenanceMode() {
        return maintenanceMode.get();
    }
    public static boolean isAdmin(UserAuth user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
    public static boolean canMakeChanges(UserAuth user) {
        if (isAdmin(user)) {
            return true;
        }
        return !isMaintenanceMode();
    }
}
