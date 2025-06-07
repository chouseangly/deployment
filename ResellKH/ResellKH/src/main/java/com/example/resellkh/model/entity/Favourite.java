package com.example.resellkh.model.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Favourite {
    private Long favouriteId;
    private Integer userId;
    private Integer productId;
    private LocalDateTime createdAt;
}
