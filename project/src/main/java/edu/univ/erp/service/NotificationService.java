package edu.univ.erp.service;

import edu.univ.erp.data.NotificationDao;
import edu.univ.erp.domain.Notification;
import java.util.List;

public class NotificationService {
    private final NotificationDao dao = new NotificationDao();
    public List<Notification> getUnreadForUser(int userId) {
        return dao.getUnreadNotificationsForUser(userId);
    }
    public boolean markAllRead(int userId) {
        return dao.markAllReadForUser(userId);
    }
}
