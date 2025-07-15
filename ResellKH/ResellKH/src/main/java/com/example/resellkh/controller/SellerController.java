// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/controller/SellerController.java
package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;
import com.example.resellkh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> createSeller(@RequestBody SellerRequest request) {
        Seller seller = sellerService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Seller profile created successfully", seller, HttpStatus.CREATED.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Seller>> getSellerByUserId(@PathVariable Long userId) {
        Seller seller = sellerService.getSellerByUserId(userId);
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("Seller information not found for this user.", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Get seller successfully", seller, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @PutMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<Seller>> updateSeller(
            @PathVariable Long sellerId,
            @RequestBody SellerRequest request) {
        try {
            Seller updatedSeller = sellerService.updateSeller(sellerId, request);
            return ResponseEntity.ok(
                    new ApiResponse<>("Seller profile updated successfully", updatedSeller, HttpStatus.OK.value(), LocalDateTime.now())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(e.getMessage(), null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }
    }
}