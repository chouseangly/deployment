package com.example.resellkh.repository;

import com.example.resellkh.model.entity.Order;
import com.example.resellkh.model.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderRepo {

    @Insert("""
        INSERT INTO orders (
            user_id, total_amount, status, payment_method, payment_reference,
            delivery_address, delivery_phone, delivery_instructions,
            created_at, updated_at
        )
        VALUES (
            #{buyerId}, #{totalAmount}, #{status}, #{paymentMethod}, #{paymentReference},
            #{deliveryAddress}, #{deliveryPhone}, #{deliveryInstructions},
            NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
    void insertOrder(Order order);

    @Insert("""
        INSERT INTO order_items (order_id, product_id, quantity, price_at_order)
        VALUES (#{orderId}, #{productId}, #{quantity}, #{priceAtOrder})
    """)
    void insertOrderItem(OrderItem item);

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "buyerId", column = "user_id"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "paymentMethod", column = "payment_method"),
            @Result(property = "paymentReference", column = "payment_reference"),
            @Result(property = "deliveryAddress", column = "delivery_address"),
            @Result(property = "deliveryPhone", column = "delivery_phone"),
            @Result(property = "deliveryInstructions", column = "delivery_instructions"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "orderItems", column = "order_id",
                    many = @Many(select = "com.example.resellkh.repository.OrderRepo.findItemsByOrderId"))
    })
    Order findOrderById(Long orderId);

    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    @Results({
            @Result(property = "orderItemId", column = "order_item_id"),
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "priceAtOrder", column = "price_at_order")
    })
    List<OrderItem> findItemsByOrderId(Long orderId);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findOrdersByUserId(Long userId);

    @Update("UPDATE orders SET status = #{status}, updated_at = NOW() WHERE order_id = #{orderId}")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);
}
