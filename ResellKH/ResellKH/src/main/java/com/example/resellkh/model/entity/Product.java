package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private Long productId;
    private String productName;
    private Long userId;
    private Long mainCategoryId;
    private String mainCategoryName;
    private Double productPrice;
    private String productStatus;
    private Double discountPercent;
    private String description;
    private String location;
    private String condition;
    private List<String> fileUrls;
    private LocalDateTime createdAt;
}
