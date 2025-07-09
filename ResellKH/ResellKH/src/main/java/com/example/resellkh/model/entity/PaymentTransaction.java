package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    private Long transactionId;
    private Long orderId;
    private String paymentProvider; // e.g. "LocalBank", "Stripe"
    private String transactionStatus; // SUCCESS, FAILED, PENDING
    private String transactionReference; // from bank/payment gateway
    private Double amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
