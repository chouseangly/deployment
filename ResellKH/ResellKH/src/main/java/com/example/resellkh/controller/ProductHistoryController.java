package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.entity.ProductHistory;
import com.example.resellkh.service.Impl.ProductHistoryServiceImpl;
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

    private final ProductHistoryServiceImpl productHistoryServiceImpl;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductHistory>>> getAllProductHistory() {
        List<ProductHistory> histories = productHistoryServiceImpl.getAllProductHistory();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get all product history successfully",
                        histories,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<List<ProductHistory>>> getHistory(@PathVariable Integer productId) {
        List<ProductHistory> historyList = productHistoryServiceImpl.getHistory(productId);

        if (historyList == null || historyList.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "No history found for product ID: " + productId,
                            List.of(),
                            HttpStatus.OK.value(),
                            LocalDateTime.now()
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get product history successfully",
                        historyList,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
}
