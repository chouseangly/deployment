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
}
