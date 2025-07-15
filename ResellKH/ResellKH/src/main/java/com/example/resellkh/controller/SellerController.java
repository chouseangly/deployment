package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;
import com.example.resellkh.repository.SellerRepo;
import com.example.resellkh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Select;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final SellerRepo sellerRepo;

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Seller>> createSeller(@RequestBody SellerRequest request) {
        Seller seller = sellerService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("create seller successfully", seller, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
    @GetMapping("/getBySellerId")
    public ResponseEntity<ApiResponse<Seller>> getSellerBySellerId(@RequestParam Long sellerId)
    {
        Seller sellers = sellerService.getSellerBySellerId(sellerId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("get seller successfully",sellers,HttpStatus.OK.value(),LocalDateTime.now())

        );
    }

}