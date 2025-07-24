package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.dto.RatingWithUserDTO;
import com.example.resellkh.model.entity.Rating;
import com.example.resellkh.repository.RatingRepo;
import com.example.resellkh.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepo ratingRepo;

    @Override
    public RatingWithUserDTO addRating(RatingRequest request) {
        Rating rating = new Rating();
        rating.setRatedUserId(request.getRatedUserId());
        rating.setRatingUserId(request.getRatingUserId());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        ratingRepo.insertRating(rating);
        return ratingRepo.getRatingWithDetailsById(rating.getRatingId());
    }

    @Override
    public List<RatingWithUserDTO> getRatingsByUserId(Long ratedUserId) {
        return ratingRepo.getRatingsWithUserDetails(ratedUserId);
    }

    @Override
    public Map<String, Object> getRatingSummaryByUserId(Long userId) {
        Double averageScore = ratingRepo.getAverageScoreByUserId(userId);
        int reviewsCount = ratingRepo.getReviewCountByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("rating", averageScore != null ? averageScore : 0.0);
        result.put("reviewsCount", reviewsCount);
        return result;
    }
}