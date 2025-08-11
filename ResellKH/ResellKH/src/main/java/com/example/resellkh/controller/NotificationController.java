package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.service.Impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationServiceImpl;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Notification>> getNotificationByUserId(@PathVariable Long userId) {
        Notification notification = notificationServiceImpl.getNotificationByUserId(userId);

        if (notification == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>("No notification found for this user", null, HttpStatus.OK.value(), LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Notification fetched successfully", notification, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<ApiResponse<Iterable<Notification>>> getAllNotificationsByUserId(@PathVariable Long userId) {
        List<Notification> notifications = notificationServiceImpl.getAllNotificationsByUserId(userId);
        if (notifications == null || notifications.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponse<>("No notification found for this user", null, HttpStatus.OK.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Notification fetched successfully", notifications, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @PutMapping("/read/{userId}/{id}")
    public ResponseEntity<Map<String, String>> markNotificationsAsRead(@PathVariable Long userId, @PathVariable Long id) {
        notificationServiceImpl.markNotificationAsRead(userId, id);
        // Return a JSON object for consistent API responses
        return ResponseEntity.ok(Map.of("message", "Notification marked as read successfully."));
    }

    @GetMapping("/getproductidbynotid/{id}")
    public ResponseEntity<ApiResponse<Long>> getProductId(@PathVariable Long id) {
        Long productId = notificationServiceImpl.getProductIdByNoId(id);

        if (productId == null) {
            // If service returns null, it means not found. Return 404.
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>("Product not fuound",null,HttpStatus.OK.value(),LocalDateTime.now())
            );
        }

        // Otherwise, return 200 OK with the product ID.
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("get product successfully",productId,HttpStatus.OK.value(),LocalDateTime.now())
        );
    }

    @DeleteMapping("/deleteallnotificationbyproductid/{productId}")
    public ResponseEntity<String> deleteAllNotificationsByProductId(@PathVariable Long productId) {
        notificationServiceImpl.deleteAllNotificationByProductId(productId);
        return ResponseEntity.ok("All notifications for product ID " + productId + " have been deleted.");
    }
}