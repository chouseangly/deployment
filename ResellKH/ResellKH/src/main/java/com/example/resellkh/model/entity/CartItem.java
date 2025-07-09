package com.example.resellkh.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItem {
    private Long cartId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Product product;
}
