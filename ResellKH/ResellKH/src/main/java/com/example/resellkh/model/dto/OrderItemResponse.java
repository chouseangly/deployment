package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private Long orderedProductId;
    private Double itemPriceAtPurchase;
    private String productName;
    private Double currentProductPrice;
    private String productCoverImageUrl;
    private Long sellerId;
}
