package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SellerRequest {
    private Long userId;
    private String businessName;
    private String businessType;
    private String businessAddress;
    private String businessDescription;
    private Double expectedRevenue;
    private String bankName;
    private String bankAccountName;
    private String bankAccountNumber;
    private LocalDateTime createAt;
}
