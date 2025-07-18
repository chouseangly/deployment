package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RatingRequest {
    private int ratedUserId;
    private int ratingUserId;
    private double score;
    private String comment;
    private LocalDateTime createdAt;

}
