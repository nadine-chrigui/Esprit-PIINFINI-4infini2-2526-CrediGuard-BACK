package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.Notification;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.NotificationCategory;

import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications(Long userId);
    void createNotification(User user, String title, String message, String reference, NotificationCategory category, String statusTag, String details);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
