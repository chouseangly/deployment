package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.NotificationRequest;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.service.Impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationServiceImpl;

    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody NotificationRequest request) {
        Notification notification = notificationServiceImpl.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Notification created successfully", notification, HttpStatus.CREATED.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Notification>> getNotificationByUserId(@PathVariable int userId) {
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
}
