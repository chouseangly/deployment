package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;
import com.example.resellkh.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final FavouriteService favouriteService;

    @PostMapping
    public ResponseEntity<ApiResponse<Favourite>> addFavourite(@RequestBody FavouriteRequest favouriteRequest) {
        Favourite favourite = favouriteService.addFavourite(favouriteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Add favourite successfully",
                        favourite,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Favourite>> remove(@RequestParam Integer userId, @RequestParam Integer productId) {
        Favourite favourite = favouriteService.removeFavourite(userId, productId);

        if (favourite == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "Favourite not found",
                            null,
                            HttpStatus.OK.value(),
                            LocalDateTime.now()
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Remove favourite successfully",
                        favourite,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<Favourite>>> getFavoriteByUserId(@PathVariable Integer userId) {
        List<Favourite> favourites = favouriteService.getFavouritesByUserId(userId);
        String message = favourites == null || favourites.isEmpty()
                ? "No favourites found for user"
                : "Get favourites by user ID successfully";

        return ResponseEntity.ok(
                new ApiResponse<>(
                        message,
                        favourites,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> check(@RequestParam Integer userId, @RequestParam Integer productId) {
        return ResponseEntity.ok(favouriteService.isFavourite(userId, productId));
    }
}
