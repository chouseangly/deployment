package com.example.resellkh.model.entity;
import com.example.resellkh.model.dto.RatingRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Rating extends RatingRequest {
    private int ratingId;
    private int ratedUserId;
    private int ratingUserId;
    private int score;
    private String comment;
}
