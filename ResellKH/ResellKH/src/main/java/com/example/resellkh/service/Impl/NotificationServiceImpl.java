package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.NotificationRequest;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.repository.NotificationRepo;
import com.example.resellkh.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepo notificationRepo;
    @Override
    public Notification createNotification(NotificationRequest request) {
       Notification notification = new Notification();
       notification.setUserId(request.getUserId());
       notification.setContent(request.getContent());
       notification.setCreatedAt(LocalDateTime.now());
       notificationRepo.createNotification(notification);
       return notification;
    }

    @Override
    public Notification getNotificationByUserId(int userId) {
        List<Notification> notifications = notificationRepo.getNotificationsByUserId(userId);
        return notifications.isEmpty() ? null : notifications.get(0);
    }
}
