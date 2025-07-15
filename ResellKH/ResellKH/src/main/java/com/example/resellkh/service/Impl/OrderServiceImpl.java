package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.DeliveryInfoDto;
import com.example.resellkh.model.dto.OrderResponse;
import com.example.resellkh.model.entity.Order;
import com.example.resellkh.model.entity.OrderItem;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.repository.OrderRepo;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepository;

    @Override
    public Order insertOrder(Order order) {
        if (order.getSubTotal() == null) {
            throw new IllegalArgumentException("SubTotal cannot be null for an order.");
        }
        if (order.getDelivery() == null) {
            order.setDelivery(0.0);
        }
        if (order.getTotalAmount() == null) {
            order.setTotalAmount(order.getSubTotal() + order.getDelivery());
        }
        orderRepository.insertOrder(order);
        return order;
    }

    @Override
    public void insertOrderItem(OrderItem orderItem) {
        orderRepository.insertOrderItem(orderItem);
    }

    @Override
    public Double findPriceByProductId(Long productId) {
        Double price = orderRepository.findPriceByProductId(productId);
        return price;
    }

    @Override
    public List<OrderResponse> getSellerOrders(Long sellerId) {
        return orderRepository.getOrdersWithItemsByBuyerId(sellerId);
    }

//    @Override
//    public Order createOrderFromCartWithDelivery(Long userId, DeliveryInfoDto deliveryInfo) {
//        List<Product> cartProducts = productRepo.getProductsInCartByUserId(userId);
//        if (cartProducts == null || cartProducts.isEmpty()) {
//            throw new IllegalArgumentException("No products in cart");
//        }
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        double totalAmount = 0.0;
//
//        for (Product product : cartProducts) {
//            OrderItem item = new OrderItem();
//            item.setProductId(product.getProductId());
//            item.setQuantity(1); // Assuming quantity 1 per cart product
//            item.setPriceAtOrder(product.getProductPrice());
//            orderItems.add(item);
//
//            totalAmount += product.getProductPrice();
//        }
//
//        Order order = new Order();
//        order.setBuyerId(userId);
//        order.setTotalAmount(totalAmount);
//        order.setStatus("PENDING");
//        order.setCreatedAt(LocalDateTime.now());
//        order.setUpdatedAt(LocalDateTime.now());
//
//        // Set delivery info
//        order.setDeliveryAddress(deliveryInfo.getDeliveryAddress());
//        order.setDeliveryPhone(deliveryInfo.getDeliveryPhone());
//        order.setDeliveryInstructions(deliveryInfo.getDeliveryInstructions());
//
//        orderRepository.insertOrder(order);
//
//        for (OrderItem item : orderItems) {
//            item.setOrderId(order.getOrderId());
//            orderRepository.insertOrderItem(item);
//        }
//
//        // Clear user cart after order creation
//        productRepo.clearCartByUserId(userId);
//
//        order.setOrderItems(orderItems);
//        return order;
//    }
//
//    @Override
//    public List<Order> getOrdersByUserId(Long userId) {
//        return orderRepository.findOrdersByUserId(userId);
//    }
//
//    @Override
//    public void updateStatus(Long orderId, String status, Long userId) {
//        if (!isValidStatus(status)) {
//            throw new IllegalArgumentException("Invalid order status: " + status);
//        }
//
//        Order order = orderRepository.findOrderById(orderId);
//        if (order == null || !order.getBuyerId().equals(userId)) {
//            throw new IllegalArgumentException("Order not found or permission denied");
//        }
//
//        orderRepository.updateOrderStatus(orderId, status);
//    }
//
//    private boolean isValidStatus(String status) {
//        return List.of("PENDING", "SHIPPED", "DELIVERED", "CANCELLED").contains(status.toUpperCase());
//    }
}
