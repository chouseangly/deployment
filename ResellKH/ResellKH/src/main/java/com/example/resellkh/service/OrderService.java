package com.example.resellkh.service;

import com.example.resellkh.model.dto.DeliveryInfoDto;
import com.example.resellkh.model.dto.OrderResponse;
import com.example.resellkh.model.entity.Order;
import com.example.resellkh.model.entity.OrderItem;

import java.util.List;

public interface OrderService {
//    Order createOrderFromCartWithDelivery(Long userId, DeliveryInfoDto deliveryInfoDto);
//    List<Order> getOrdersByUserId(Long userId);
//    void updateStatus(Long orderId, String status, Long userId);
    Order insertOrder(Order order);
    void insertOrderItem(OrderItem orderItem);
    Double findPriceByProductId(Long productId);
    // Method to get orders for a seller, with nested items
    List<OrderResponse> getSellerOrders(Long sellerId);
    int countAllProductByUserId(Long userId);
    List<OrderResponse> getSellerOrdersByOrderId(Long orderId);
}
