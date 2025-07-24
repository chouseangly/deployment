// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/controller/FavouriteController.java
package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.dto.NotificationFavorite;
import com.example.resellkh.model.entity.Favourite;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.service.FavouriteService;
import com.example.resellkh.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Ensure this is imported
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final NotificationService notificationService;

    /**
     * ✅ PERMANENT FIX: This method is now wrapped in a @Transactional annotation.
     * This makes the entire operation atomic. If any part fails, the whole process
     * is rolled back, guaranteeing no orphaned notifications can be created.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Favourite>> addFavourite(@RequestBody FavouriteRequest favouriteRequest) {
        // Step 1: Add the product to the user's favourites.
        Favourite favourite = favouriteService.addFavourite(favouriteRequest);
        Long productId = favouriteRequest.getProductId();

        // Step 2: Get details for the notification content.
        NotificationFavorite notificationFavorite = notificationService.favoriteNotification(productId);

        // Step 3: Build the notification object, including the productId from the start.
        Notification notification = Notification.builder()
                .userId(favourite.getUserId())
                .productId(productId) // CRITICAL: The productId is now part of the initial object.
                .title("Favourite")
                .content(notificationFavorite.getDescription())
                .iconUrl(notificationFavorite.getProfileImage())
                .build();

        // Step 4: Create the complete notification in a single, safe database call.
        notificationService.createNotificationWithType(notification);

        // The old, unsafe two-step "insert then update" logic is no longer needed.

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Add favourite successfully",
                        favourite,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }

    // No changes needed for the methods below.
    @DeleteMapping
    public ResponseEntity<ApiResponse<Favourite>> remove(@RequestParam Long userId, @RequestParam Long productId) {
        Favourite favourite = favouriteService.removeFavourite(userId, productId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        favourite != null ? "Remove favourite successfully" : "Favourite not found",
                        favourite,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/with-products/{userId}")
    public ResponseEntity<ApiResponse<List<Favourite>>> getFavouritesWithProductsByUserId(@PathVariable Long userId) {
        List<Favourite> favourites = favouriteService.getFavouritesWithProductsByUserId(userId);
        String message = (favourites == null || favourites.isEmpty())
                ? "No favourites found for user"
                : "Get favourites with products by user ID successfully";

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
    public ResponseEntity<Boolean> check(@RequestParam Long userId, @RequestParam Long productId) {
        return ResponseEntity.ok(favouriteService.isFavourite(userId, productId));
    }
}