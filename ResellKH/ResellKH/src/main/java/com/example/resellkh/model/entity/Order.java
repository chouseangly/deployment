package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long orderId;
    private Long buyerId;
    private Long sellerId; // Optional for multi-seller marketplaces
    private Double totalAmount;
    private String status;

    // Delivery info
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryInstructions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional for response purpose
    private List<OrderItem> orderItems;
}
