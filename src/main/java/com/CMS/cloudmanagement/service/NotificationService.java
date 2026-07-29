package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.Notification;
import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(User user, String message, String type) {
        Notification notification = new Notification(user, message, type);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotificationsForUser(User user) {
        return notificationRepository.findByUserAndReadStatusOrderByCreatedAtDesc(user, "UNREAD");
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndReadStatusOrderByCreatedAtDesc(user, "UNREAD");
        for (Notification n : unread) {
            n.setReadStatus("READ");
        }
        notificationRepository.saveAll(unread);
    }
}
