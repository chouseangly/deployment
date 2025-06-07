package com.example.resellkh.service;

import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.entity.Rating;

import java.util.List;

public interface RatingService {
    Rating addRating(RatingRequest request);
    List<Rating> getRatingsByUserId(int ratedUserId);
}
