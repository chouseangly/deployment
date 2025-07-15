package com.example.resellkh.controller;

import com.example.resellkh.jwt.JwtService;
import com.example.resellkh.model.dto.*;
import com.example.resellkh.model.entity.Order;
import com.example.resellkh.model.entity.OrderItem;
import com.example.resellkh.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        return jwtService.extractUserId(token);
    }

    @PostMapping("/insertorder")
    public ResponseEntity<ApiResponse<Order>> insert(@RequestBody OrderRequest orderRequest) {
        if (orderRequest.getBuyerId() == null || orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Order>builder()
                            .message("buyerId and orderItems are required")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .timestamp(LocalDateTime.now())
                            .build()
            );

        }

        Order order = new Order();
        order.setBuyerId(orderRequest.getBuyerId());
        order.setFullName(orderRequest.getFullName());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setAddress(orderRequest.getAddress());
        order.setSubTotal(orderRequest.getSubTotal());
        order.setDelivery(orderRequest.getDelivery());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setCreatedAt(LocalDateTime.now());

        Order orders = orderService.insertOrder(order);

        for (OrderItemRequest itemReq : orderRequest.getOrderItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(orders.getOrderId());
            item.setProductId(itemReq.getProductId());
            Double price = itemReq.getPrice();
            item.setPrice(price);
            item.setSellerId(itemReq.getSellerId());

            orderService.insertOrderItem(item);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Order>builder()
                        .message("Order created successfully")
                        .payload(order)
                        .status(HttpStatus.CREATED.value())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(@PathVariable Long sellerId) {
        try {
            List<OrderResponse> sellerOrders = orderService.getSellerOrders(sellerId);
            if (sellerOrders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(sellerOrders, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching seller orders: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostMapping("/checkout")
//    public ResponseEntity<ApiResponse<Order>> checkoutOrder(
//            HttpServletRequest request,
//            @RequestBody DeliveryInfoDto deliveryInfoDto
//    ) {
//        String token = extractToken(request);
//        Long userId = getUserIdFromToken(token);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    ApiResponse.<Order>builder()
//                            .message("Unauthorized")
//                            .status(HttpStatus.UNAUTHORIZED.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        }
//
//        try {
//            Order order = orderService.createOrderFromCartWithDelivery(userId, deliveryInfoDto);
//            return ResponseEntity.ok(
//                    ApiResponse.<Order>builder()
//                            .message("Order created successfully")
//                            .payload(order)
//                            .status(HttpStatus.OK.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    ApiResponse.<Order>builder()
//                            .message("Error: " + e.getMessage())
//                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(HttpServletRequest request) {
//        String token = extractToken(request);
//        Long userId = getUserIdFromToken(token);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    ApiResponse.<List<Order>>builder()
//                            .message("Unauthorized")
//                            .status(HttpStatus.UNAUTHORIZED.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        }
//
//        List<Order> orders = orderService.getOrdersByUserId(userId);
//        return ResponseEntity.ok(
//                ApiResponse.<List<Order>>builder()
//                        .message("Order list fetched successfully")
//                        .payload(orders)
//                        .status(HttpStatus.OK.value())
//                        .timestamp(LocalDateTime.now())
//                        .build()
//        );
//    }
//
//    @PutMapping("/{orderId}/status")
//    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
//            @PathVariable Long orderId,
//            @RequestParam String status,
//            HttpServletRequest request
//    ) {
//        String token = extractToken(request);
//        Long userId = getUserIdFromToken(token);
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
//                    ApiResponse.<String>builder()
//                            .message("Unauthorized")
//                            .status(HttpStatus.UNAUTHORIZED.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        }
//
//        try {
//            orderService.updateStatus(orderId, status.toUpperCase(), userId);
//            return ResponseEntity.ok(
//                    ApiResponse.<String>builder()
//                            .message("Order status updated successfully")
//                            .payload("Updated to: " + status.toUpperCase())
//                            .status(HttpStatus.OK.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(
//                    ApiResponse.<String>builder()
//                            .message("Invalid status")
//                            .status(HttpStatus.BAD_REQUEST.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    ApiResponse.<String>builder()
//                            .message("Error: " + e.getMessage())
//                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                            .timestamp(LocalDateTime.now())
//                            .build()
//            );
//        }
//    }
}
