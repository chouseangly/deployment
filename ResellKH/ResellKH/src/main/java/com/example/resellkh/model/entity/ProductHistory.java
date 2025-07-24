package com.example.resellkh.model.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistory {
    private Long id; // Changed from Integer
    private Long productId; // Changed from Integer
    private String message;
    private LocalDateTime updatedAt;
}