package com.example.resellkh.model.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private Double price;
    private Long sellerId;
}
