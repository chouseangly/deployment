package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProductRequest {
    private String productName;
    private Long userId;
    private String categoryName;
    private Double productPrice;
    private Double discountPercent;
    private String productStatus;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private String condition;

}
