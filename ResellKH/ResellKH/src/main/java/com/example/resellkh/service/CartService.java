package com.example.resellkh.service;

import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.CartItem;

import java.util.List;

public interface CartService {
    ProductWithFilesDto addProductToCart(Long userId, Long productId, Integer quantity);
    List<CartItem> getCartItems(Long userId);
    void clearCart(Long userId);
    void removeProduct(Long userId, Long productId);
}