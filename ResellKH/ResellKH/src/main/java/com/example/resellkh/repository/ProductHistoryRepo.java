package com.example.resellkh.repository;
import com.example.resellkh.model.entity.ProductHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductHistoryRepo {

    @Insert("INSERT INTO product_history (product_id, message, updated_at) VALUES (#{productId}, #{message}, #{updatedAt})")
    void addHistory(ProductHistory history);

    @Select("SELECT * FROM product_history WHERE product_id = #{productId} ORDER BY updated_at DESC")
    List<ProductHistory> getHistoryByProductId(Long productId);

    @Select("SELECT * FROM product_history")
    List<ProductHistory> getAllProductHistory();
}