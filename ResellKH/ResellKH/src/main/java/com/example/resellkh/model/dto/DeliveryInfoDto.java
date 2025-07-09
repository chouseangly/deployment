package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryInfoDto {
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryInstructions;
}
