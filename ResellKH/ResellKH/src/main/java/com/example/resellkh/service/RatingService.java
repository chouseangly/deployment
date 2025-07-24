package com.example.resellkh.service;

import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.dto.RatingWithUserDTO;

import java.util.List;
import java.util.Map;

public interface RatingService {
    RatingWithUserDTO addRating(RatingRequest request);
    List<RatingWithUserDTO> getRatingsByUserId(Long ratedUserId);
    Map<String, Object> getRatingSummaryByUserId(Long userId);
}