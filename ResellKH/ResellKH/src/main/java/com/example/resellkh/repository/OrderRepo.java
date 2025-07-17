package com.example.resellkh.repository;

import com.example.resellkh.model.dto.OrderItemResponse;
import com.example.resellkh.model.dto.OrderResponse;
import com.example.resellkh.model.entity.Order;
import com.example.resellkh.model.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderRepo {

    @Insert("INSERT INTO orders (buyer_id, full_name, phone_number, address, sub_total, delivery, total_amount, created_at) " +
            "VALUES (#{buyerId}, #{fullName}, #{phoneNumber}, #{address}, #{subTotal}, #{delivery}, #{totalAmount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
    int insertOrder(Order order);

    @Insert("INSERT INTO order_items (order_id, product_id, price, seller_id) " +
            "VALUES (#{orderId}, #{productId}, #{price}, #{sellerId})")
    void insertOrderItem(OrderItem item);

    @Select("SELECT price FROM products WHERE product_id = #{productId}")
    Double findPriceByProductId(@Param("ProductId") Long ProductId);

    @Results(id = "buyerOrderItemSubMap", value = {
            @Result(property = "orderItemId", column = "order_item_id", id = true),
            @Result(property = "orderedProductId", column = "orderProductId"),
            @Result(property = "itemPriceAtPurchase", column = "itemPriceAtPurchase"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "currentProductPrice", column = "currentProductPrice"),
            @Result(property = "productCoverImageUrl", column = "productCoverImageUrl"),
            @Result(property = "sellerId", column = "sellerId")
    })
    @Select("SELECT 1") // dummy query just to register the result map
    OrderItemResponse dummy();

    @Results(id = "buyerOrderCombinedResultMap", value = {
            @Result(property = "orderId", column = "order_id", id = true),
            @Result(property = "buyerFullName", column = "buyerFullName"),
            @Result(property = "buyerPhoneNumber", column = "buyerPhoneNumber"),
            @Result(property = "buyerAddress", column = "buyerAddress"),
            @Result(property = "buyerEmail", column = "buyerEmail"),
            @Result(property = "orderSubTotal", column = "orderSubTotal"),
            @Result(property = "orderDeliveryCharge", column = "orderDeliveryCharge"),
            @Result(property = "orderTotalAmount", column = "orderTotalAmount"),
            @Result(property = "orderCreatedAt", column = "orderCreatedAt"),
            @Result(property = "sellerId", column = "sellerId"),
            @Result(property = "sellerFirstName", column = "sellerFirstName"),
            @Result(property = "sellerLastName", column = "sellerLastName"),
            @Result(property = "sellerUsername", column = "sellerUserName"),
            @Result(property = "sellerProfileImageUrl", column = "profile_image"),
            @Result(property = "orderItems", javaType = List.class, column = "order_id",
                    many = @Many(resultMap = "buyerOrderItemSubMap")) // Reference the sub-map
    })
    @Select("SELECT " +
            "    o.order_id , " +
            "    o.full_name AS buyerFullName, " +
            "    o.phone_number AS buyerPhoneNumber , " +
            "    o.address AS buyerAddress , " +
            "    o.sub_total AS orderSubTotal, " +
            "    o.delivery AS orderDeliveryCharge , " +
            "    o.total_amount AS orderTotalAmount, " +
            "    o.created_at AS orderCreatedAt, " +
            "    b.email AS buyerEmail, " +

            "    oi.order_item_id , " +
            "    oi.product_id AS orderProductId, " +
            "    oi.price AS itemPriceAtPurchase, " +
            "    oi.seller_id , " +

            "    p.product_name , " +
            "    p.product_price AS currentProductPrice , " +

            "    pi.url AS productCoverImageUrl, " +

            "    s.user_id AS sellerId, " +
            "    s.first_name AS sellerFirstName, " +
            "    s.last_name AS sellerLastName, " +
            "    sp.user_name AS sellerUserName, " +
            "    sp.profile_image " +
            "FROM " +
            "    orders o " +
            "JOIN " +
            "    users b ON o.buyer_id = b.user_id " +
            "JOIN " +
            "    order_items oi ON o.order_id = oi.order_id " +
            "JOIN " +
            "    products p ON oi.product_id = p.product_id " +
            "JOIN " +
            "    users s ON p.user_id = s.user_id " +
            "LEFT JOIN " +
            "    user_profile sp ON s.user_id = sp.user_id " +
            "LEFT JOIN " +
            "    product_images pi ON p.product_id = pi.product_id AND pi.id = ( " +
            "        SELECT MIN(id) FROM product_images WHERE product_id = p.product_id " +
            "    ) " +
            "WHERE " +
            "    o.buyer_id = #{sellerId} " +
            "ORDER BY " +
            "    o.created_at DESC, oi.order_item_id")
    List<OrderResponse> getOrdersWithItemsByBuyerId(@Param("sellerId") Long sellerId); // This method uses the map

    @Select("SELECT COUNT(*) FROM products WHERE user_id = #{userId}")
    int countAllProductByUserId(@Param("userId") Long userId);

    // This method now *references* the already defined result map by its ID
    @ResultMap("buyerOrderCombinedResultMap") // <--- Referencing the existing map
    @Select("SELECT " +
            "    o.order_id , " +
            "    o.full_name AS buyerFullName, " +
            "    o.phone_number AS buyerPhoneNumber , " +
            "    o.address AS buyerAddress , " +
            "    o.sub_total AS orderSubTotal, " +
            "    o.delivery AS orderDeliveryCharge , " +
            "    o.total_amount AS orderTotalAmount, " +
            "    o.created_at AS orderCreatedAt, " +
            "    b.email AS buyerEmail, " +

            "    oi.order_item_id , " +
            "    oi.product_id AS orderProductId, " +
            "    oi.price AS itemPriceAtPurchase, " +
            "    oi.seller_id , " +

            "    p.product_name , " +
            "    p.product_price AS currentProductPrice , " +

            "    pi.url AS productCoverImageUrl, " +

            "    s.user_id AS sellerId, " +
            "    s.first_name AS sellerFirstName, " +
            "    s.last_name AS sellerLastName, " +
            "    sp.user_name AS sellerUserName " +
            "FROM " +
            "    orders o " +
            "JOIN " +
            "    users b ON o.buyer_id = b.user_id " +
            "JOIN " +
            "    order_items oi ON o.order_id = oi.order_id " +
            "JOIN " +
            "    products p ON oi.product_id = p.product_id " +
            "JOIN " +
            "    users s ON p.user_id = s.user_id " +
            "LEFT JOIN " +
            "    user_profile sp ON s.user_id = sp.user_id " +
            "LEFT JOIN " +
            "    product_images pi ON p.product_id = pi.product_id AND pi.id = ( " +
            "        SELECT MIN(id) FROM product_images WHERE product_id = p.product_id " +
            "    ) " +
            "WHERE " +
            "    o.order_id = #{orderId} " +
            "ORDER BY " +
            "    o.created_at DESC, oi.order_item_id")
    List<OrderResponse> getOrdersWithItemsByOrderId(@Param("orderId") Long orderId); // This method also uses the map


}
