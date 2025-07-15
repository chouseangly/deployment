package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDraft {
    private Long draftId;
    private String productName;
    private Long userId;
    private Long mainCategoryId;
    private String categoryName;
    private Double productPrice;
    private Double discountPercent;
    private String productStatus; // default to "draft"
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private String condition;
    private String telegramUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> fileUrls;  // URLs of draft images


}
