package com.example.resellkh.model.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistory {
    private Integer id;
    private Integer productId;
    private String message;
    private LocalDateTime updatedAt;
}
