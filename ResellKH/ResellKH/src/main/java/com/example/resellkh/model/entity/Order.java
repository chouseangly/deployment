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
    private String fullName;
    private String phoneNumber;
    private String address;
    private Double subTotal;
    private Double delivery;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> orderItems;
}
