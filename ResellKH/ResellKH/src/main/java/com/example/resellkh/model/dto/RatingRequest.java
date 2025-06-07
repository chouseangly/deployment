package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RatingRequest {
    private int ratedUserId;
    private int ratingUserId;
    private int score;
    private String comment;
}
