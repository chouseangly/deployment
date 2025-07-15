package com.example.resellkh.controller;

import com.example.resellkh.jwt.JwtService;
import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.CartItem;
import com.example.resellkh.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtService jwtService;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);
        }
        return null;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> addProductToCart(
            HttpServletRequest request,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<ProductWithFilesDto>builder()
                            .message("Unauthorized")
                            .status(401)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
        ProductWithFilesDto product = cartService.addProductToCart(userId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.<ProductWithFilesDto>builder()
                .message("Product added to cart")
                .payload(product)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<List<CartItem>>builder()
                            .message("Unauthorized")
                            .status(401)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
        List<CartItem> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(ApiResponse.<List<CartItem>>builder()
                .message("Cart fetched")
                .payload(items)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CartItem>>> getCartByUserId(
            HttpServletRequest request,
            @PathVariable Long userId
    ) {
        // First verify the requesting user is authorized (admin or the same user)
        Long requestingUserId = getUserIdFromRequest(request);
        if (requestingUserId == null || (!requestingUserId.equals(userId) && !isAdmin(request))) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<List<CartItem>>builder()
                            .message("Forbidden - Unauthorized access")
                            .status(403)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        List<CartItem> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(ApiResponse.<List<CartItem>>builder()
                .message("Cart fetched for user " + userId)
                .payload(items)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<String>builder()
                            .message("Unauthorized")
                            .status(401)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Cart cleared")
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<String>> removeProductFromCart(
            HttpServletRequest request,
            @RequestParam Long productId
    ) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<String>builder()
                            .message("Unauthorized")
                            .status(401)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
        cartService.removeProduct(userId, productId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Product removed from cart")
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    private boolean isAdmin(HttpServletRequest request) {
        // Implement your admin check logic here
        // This might involve checking the user's role from the JWT token
        // For example:
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String role = jwtService.extractUsername(token); // Assuming you have this method
            return "ADMIN".equals(role);
        }
        return false;
    }
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getCartItemCount(
            HttpServletRequest request,
            @RequestParam(required = false) Long userId // Allow userId as query param, but prefer from token
    ) {
        Long authenticatedUserId = getUserIdFromRequest(request);

        // If a userId is provided in query, ensure it matches authenticated user OR authenticated user is admin
        if (userId != null) {
            if (authenticatedUserId == null || (!authenticatedUserId.equals(userId) && !isAdmin(request))) {
                return ResponseEntity.status(403).body(
                        ApiResponse.<Map<String, Integer>>builder()
                                .message("Forbidden - Unauthorized to view this cart count")
                                .status(403)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
            }
        } else { // No userId in query param, use authenticated user's ID
            if (authenticatedUserId == null) {
                return ResponseEntity.status(401).body(
                        ApiResponse.<Map<String, Integer>>builder()
                                .message("Unauthorized - No user ID found from token or query")
                                .status(401)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
            }
            userId = authenticatedUserId; // Use the ID from the token
        }

        int count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder()
                .message("Cart item count fetched successfully")
                .payload(Map.of("count", count)) // Return as a map { "count": N }
                .status(200)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

}