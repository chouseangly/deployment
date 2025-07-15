package com.example.resellkh.service.Impl;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.CartItem;
import com.example.resellkh.repository.CartRepo;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;
    private final ProductRepo productRepo;

    @Override
    public ProductWithFilesDto addProductToCart(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) quantity = 1;
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        cartRepo.addProductToCart(item);
        return productRepo.findProductDtoById(productId);
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        return cartRepo.findCartItemsByUserId(userId);
    }

    @Override
    public void clearCart(Long userId) {
        cartRepo.clearCartByUserId(userId);
    }

    @Override
    public void removeProduct(Long userId, Long productId) {
        cartRepo.removeProductFromCart(userId, productId);
    }

    @Override
    public int getCartItemCount(Long userId) {
        return cartRepo.getCartItemCount(userId);
    }

}
