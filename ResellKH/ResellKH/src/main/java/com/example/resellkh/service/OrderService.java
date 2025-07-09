package com.example.resellkh.service;

import com.example.resellkh.model.dto.DeliveryInfoDto;
import com.example.resellkh.model.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrderFromCartWithDelivery(Long userId, DeliveryInfoDto deliveryInfoDto);
    List<Order> getOrdersByUserId(Long userId);
    void updateStatus(Long orderId, String status, Long userId);
}
