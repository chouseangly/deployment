package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Rating {
    private Long ratingId; // Changed from int
    private Long ratedUserId; // Changed from int
    private Long ratingUserId; // Changed from int
    private double score;
    private String comment;
    private LocalDateTime createdAt;
}