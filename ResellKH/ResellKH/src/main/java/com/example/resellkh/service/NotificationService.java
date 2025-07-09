package com.example.resellkh.service;

import com.example.resellkh.model.dto.NotificationRequest;
import com.example.resellkh.model.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(NotificationRequest request);

    Notification getNotificationByUserId(int userId);
    List<Notification> getAllNotificationsByUserId(int userId);
    void markNotificationAsRead(int userId, int id);
    void createNotificationWithType(Notification notification);
}
