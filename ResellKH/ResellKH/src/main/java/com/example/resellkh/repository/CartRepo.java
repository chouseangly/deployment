package com.example.resellkh.repository;

import com.example.resellkh.model.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartRepo {

    @Insert("""
        INSERT INTO cart (user_id, product_id, quantity)
        VALUES (#{userId}, #{productId}, #{quantity})
        ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart.quantity + EXCLUDED.quantity
    """)
    void addProductToCart(CartItem cartItem);

    @Select("""
    SELECT 
        c.cart_id, c.user_id, c.quantity, c.product_id,
        p.product_id AS p_id, p.product_name, p.product_price, 
        p.user_id AS product_user_id, p.main_category_id,
        p.discount_percent, p.product_status, p.description,
        p.location, p.condition, p.latitude, p.longitude,
        p.created_at, p.telegram_url,
        cat.name AS category_name
    FROM cart c
    JOIN products p ON c.product_id = p.product_id
    JOIN main_category cat ON p.main_category_id = cat.main_category_id
    WHERE c.user_id = #{userId}
""")
    @Results(id = "cartWithProductMap", value = {
            @Result(property = "cartId", column = "cart_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "product.productId", column = "p_id"),
            @Result(property = "product.productName", column = "product_name"),
            @Result(property = "product.userId", column = "product_user_id"),
            @Result(property = "product.mainCategoryId", column = "main_category_id"),
            @Result(property = "product.categoryName", column = "category_name"),
            @Result(property = "product.productPrice", column = "product_price"),
            @Result(property = "product.discountPercent", column = "discount_percent"),
            @Result(property = "product.productStatus", column = "product_status"),
            @Result(property = "product.description", column = "description"),
            @Result(property = "product.location", column = "location"),
            @Result(property = "product.condition", column = "condition"),
            @Result(property = "product.latitude", column = "latitude"),
            @Result(property = "product.longitude", column = "longitude"),
            @Result(property = "product.createdAt", column = "created_at"),
            @Result(property = "product.telegramUrl", column = "telegram_url"),
            @Result(property = "product.fileUrls", column = "p_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<CartItem> findCartItemsByUserId(Long userId);

    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    void clearCartByUserId(Long userId);

    @Delete("""
        DELETE FROM cart
        WHERE user_id = #{userId} AND product_id = #{productId}
    """)
    void removeProductFromCart(@Param("userId") Long userId, @Param("productId") Long productId);
}