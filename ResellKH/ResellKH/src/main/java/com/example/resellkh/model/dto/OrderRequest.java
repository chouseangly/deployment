package com.example.resellkh.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {
    private Long buyerId;
    private String fullName;
    private String phoneNumber;
    private String address;
    private Double subTotal;
    private Double delivery;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemRequest> orderItems;
}
