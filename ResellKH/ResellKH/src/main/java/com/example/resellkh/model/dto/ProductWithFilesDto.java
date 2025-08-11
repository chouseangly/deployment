package com.example.resellkh.model.dto;

import com.example.resellkh.model.entity.ProductDraft;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithFilesDto extends ProductDraft {
    private Long productId;
    private String productName;
    private Long userId;
    private Long mainCategoryId;
    private String categoryName;
    private Double productPrice;
    private Double discountPercent;
    private String productStatus;
    private String description;
    private String location;
    private String telegramUrl;
    private String condition;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private List<ProductFile> media;

}
