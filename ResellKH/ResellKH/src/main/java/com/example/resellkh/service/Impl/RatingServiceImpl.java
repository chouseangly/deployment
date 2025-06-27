package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.entity.Rating;
import com.example.resellkh.repository.RatingRepo;
import com.example.resellkh.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepo ratingRepo;

    @Override
    public Rating addRating(RatingRequest request) {
        Rating rating = new Rating();
        rating.setRatedUserId(request.getRatedUserId());
        rating.setRatingUserId(request.getRatingUserId());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        ratingRepo.insertRating(rating);
        Rating savedRating = ratingRepo.getRatingById(rating.getRatingId());
        return savedRating != null ? savedRating : rating;
    }


    @Override
    public List<Rating> getRatingsByUserId(int ratedUserId) {
        return ratingRepo.getRatingsByUserId(ratedUserId);
    }


}
