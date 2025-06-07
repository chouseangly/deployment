package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FavouriteRequest {
    private Integer userId;
    private Integer productId;
    private LocalDateTime createdAt;
}
