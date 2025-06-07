package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.entity.Rating;
import com.example.resellkh.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Rating>> addRating(@RequestBody RatingRequest request) {
        Rating rating = ratingService.addRating(request);

        ApiResponse<Rating> response = new ApiResponse<>(
                "Rating added successfully",
                rating,
                HttpStatus.CREATED.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ratedUserId}")
    public ResponseEntity<ApiResponse<List<Rating>>> getRatingsByUserId(@PathVariable int ratedUserId) {
        List<Rating> ratings = ratingService.getRatingsByUserId(ratedUserId);

        if (ratings == null || ratings.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "No ratings found for user ID: " + ratedUserId,
                            List.of(),
                            HttpStatus.OK.value(),
                            LocalDateTime.now()
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Fetched ratings successfully",
                        ratings,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
}
