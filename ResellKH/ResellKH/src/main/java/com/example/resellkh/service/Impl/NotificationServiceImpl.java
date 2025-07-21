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
import java.util.Optional;

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

    @Override
    public List<Notification> getAllNotificationsByUserId(int userId) {
        return notificationRepo.getAllNotificationsByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(int userId, int id) {
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
    public int insertproductId(int productId, int id) {
        return notificationRepo.insertProductId(productId, id);
    }

    @Override
    public Integer getProductIdByNoId(int id) { // Changed from int to Integer
        return notificationRepo.getProductIdByNoId(id);
    }

}