package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.entity.ProductHistory;
import com.example.resellkh.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product-history")
@RequiredArgsConstructor
public class ProductHistoryController {

    private final ProductHistoryService productHistoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<List<ProductHistory>>> getHistory(@PathVariable Long productId) {
        List<ProductHistory> historyList = productHistoryService.getHistory(productId);
        String message = (historyList == null || historyList.isEmpty()) ? "No history found" : "Get product history successfully";
        return ResponseEntity.ok(
                new ApiResponse<>(message, historyList, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}