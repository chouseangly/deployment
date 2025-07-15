package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller {
    private Long sellerId;
    private Long userId;
    private String businessName;
    private String businessType;
    private String businessAddress;
    private String businessDescription;
    private Double expectedRevenue;
    private String bankName;
    private String bankAccountName;
    private String bankAccountNumber;
    private LocalDateTime createdAt;
}