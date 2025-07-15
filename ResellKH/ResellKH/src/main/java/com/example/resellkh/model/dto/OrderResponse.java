package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    // Order Details
    private Long orderId;
    private String buyerFullName;
    private String buyerPhoneNumber;
    private String buyerAddress;
    private String buyerEmail;
    private Double orderSubTotal;
    private Double orderDeliveryCharge;
    private Double orderTotalAmount;
    private LocalDateTime orderCreatedAt;

    // Seller Details
    private Long sellerId;
    private String sellerFirstName;
    private String sellerLastName;
    private String sellerUsername;
    private String sellerProfileImageUrl;

    // List of items in this order
    private List<OrderItemResponse> orderItems = new ArrayList<>();
}
