package com.example.resellkh.service;

import com.example.resellkh.model.dto.NotificationRequest;
import com.example.resellkh.model.entity.Notification;

public interface NotificationService {
    Notification createNotification(NotificationRequest request);

    Notification getNotificationByUserId(int userId);
}
