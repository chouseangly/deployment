package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long productId;
    private String productName;
    private Long userId;
    private Long mainCategoryId;
    private String categoryName;  // This should match the column name in your SQL
    private Double productPrice;
    private Double discountPercent;
    private String productStatus;
    private String description;
    private List<String> fileUrls;
    private String location;
    private String condition;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private String telegramUrl;
}