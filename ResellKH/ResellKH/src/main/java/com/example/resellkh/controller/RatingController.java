package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.dto.RatingWithUserDTO;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.service.NotificationService;
import com.example.resellkh.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingWithUserDTO>> addRating(@RequestBody RatingRequest request) {
        RatingWithUserDTO rating = ratingService.addRating(request);
        LocalDateTime createdAt = LocalDateTime.now();
        String description = rating.getReviewerName() + " rated you " + request.getScore() + " stars"
                + " on " + createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ". "
                + "Comment: \"" + request.getComment() + "\"";

        Notification notification = Notification.builder()
                .userId(request.getRatedUserId())
                .title("Rating")
                .content(description)
                .iconUrl(rating.getReviewerAvatar())
                .build();
        notificationService.createNotificationWithType(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Add rating successfully", rating, HttpStatus.CREATED.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/{ratedUserId}")
    public ResponseEntity<ApiResponse<List<RatingWithUserDTO>>> getRatingsByUserId(@PathVariable Long ratedUserId) {
        List<RatingWithUserDTO> ratings = ratingService.getRatingsByUserId(ratedUserId);
        return ResponseEntity.ok(
                new ApiResponse<>("Get ratings by user ID successfully", ratings, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRatingSummary(@PathVariable Long userId) {
        Map<String, Object> summary = ratingService.getRatingSummaryByUserId(userId);
        return ResponseEntity.ok(
                new ApiResponse<>("Fetched rating summary successfully", summary, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}