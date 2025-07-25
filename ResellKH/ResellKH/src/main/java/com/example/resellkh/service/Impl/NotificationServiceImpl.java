package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.NotificationFavorite;
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
        notification.setUserId((long) request.getUserId());
        notification.setContent(request.getContent());
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.createNotificationWithType(notification);
        return notification;
    }

    @Override
    public Notification getNotificationByUserId(Long userId) {
        List<Notification> notifications = notificationRepo.getNotificationsByUserId(userId);
        return notifications.isEmpty() ? null : notifications.get(0);
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(Long userId) {
        return notificationRepo.getAllNotificationsByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(Long userId, Long id) {
        notificationRepo.markNotificationAsRead(userId, id);
    }

    @Override
    public void createNotificationWithType(Notification notification) {
        notificationRepo.createNotificationWithType(notification);
    }

    @Override
    public NotificationFavorite favoriteNotification(Long productId) {
        return notificationRepo.getFavoriteNotification(productId);
    }

    @Override
    public Long insertproductId(Long productId, Long id) {
        return (long) notificationRepo.getProductIdByNoId(productId);
    }

    @Override
    public Long getProductIdByNoId(Long id) { // Changed from int to Integer
        return Long.valueOf(notificationRepo.getProductIdByNoId(id));
    }

    @Override
    public void deleteAllNotificationByProductId(Long productId) {
        notificationRepo.deleteAllNotificationsByProductId(productId);
    }


}