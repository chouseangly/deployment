package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.RatingRequest;
import com.example.resellkh.model.dto.RatingWithUserDTO;
import com.example.resellkh.model.entity.Rating;
import com.example.resellkh.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingWithUserDTO>> addRating(@RequestBody RatingRequest request) {
        RatingWithUserDTO rating = ratingService.addRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Add rating successfully",
                        rating,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/{ratedUserId}")
    public ResponseEntity<ApiResponse<List<RatingWithUserDTO>>> getRatingsByUserId(@PathVariable int ratedUserId) {
        List<RatingWithUserDTO> ratings = ratingService.getRatingsByUserId(ratedUserId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        "Get ratings by user ID successfully",
                        ratings,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
    @GetMapping()
    public ResponseEntity<ApiResponse<List<RatingWithUserDTO>>> getAllRatings() {
        try {
            List<RatingWithUserDTO> ratings = ratingService.getAllRating();

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "All ratings retrieved successfully",
                            ratings,
                            HttpStatus.OK.value(),
                            LocalDateTime.now()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(
                            "Failed to retrieve ratings: " + e.getMessage(),
                            null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            LocalDateTime.now()
                    )
            );
        }
    }
    @GetMapping("/summary/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRatingSummary(@PathVariable int userId) {
        Map<String, Object> summary = ratingService.getRatingSummaryByUserId(userId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Fetched rating summary successfully",
                        summary,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

}