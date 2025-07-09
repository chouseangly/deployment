package com.example.resellkh.service;

import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.dto.RatingWithUserDTO;
import com.example.resellkh.model.entity.Rating;

import java.util.List;
import java.util.Map;

public interface RatingService {
      List<RatingWithUserDTO> getAllRating();

    RatingWithUserDTO addRating(RatingRequest request);
    List<RatingWithUserDTO> getRatingsByUserId(int ratedUserId);

    Map<String, Object> getRatingSummaryByUserId(int userId);
}
