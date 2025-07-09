package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Rating {
    private int ratingId;
    private int ratedUserId;
    private int ratingUserId;
    private double score;
    private String comment;
    private LocalDateTime createdAt;
    private String reviewerName;
    private String reviewerAvatar;
}
