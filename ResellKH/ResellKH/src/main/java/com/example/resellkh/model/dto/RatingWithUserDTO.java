package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingWithUserDTO {
    private Long ratingId; // Changed from int
    private Long ratedUserId; // Changed from int
    private Long ratingUserId; // Changed from int
    private double score;
    private String comment;
    private LocalDateTime createdAt;
    private String reviewerName;
    private String reviewerAvatar;
}