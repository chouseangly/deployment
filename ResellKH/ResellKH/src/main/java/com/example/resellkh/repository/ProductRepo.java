package com.example.resellkh.repository;

import com.example.resellkh.model.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductRepo {

    @Insert("""
        INSERT INTO products (
            product_name, user_id, main_category_id, product_price, discount_percent,
            product_status, description, location, condition, latitude, longitude, created_at
        ) VALUES (
            #{productName}, #{userId}, #{mainCategoryId}, #{productPrice}, #{discountPercent},
            #{productStatus}, #{description}, #{location}, #{condition}, #{latitude}, #{longitude}, #{createdAt}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "productId", keyColumn = "product_id")
    void insertProduct(Product product);


    @Select("SELECT main_category_id FROM main_category WHERE name = #{name}")
    Integer getCategoryIdByName(String name);

    @Results(id = "ProductResult", value = {
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent",column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at")
    })
    @Select("SELECT * FROM products WHERE product_id = #{id}")
    Product findById(Long id);

    @Select("SELECT name FROM main_category WHERE main_category_id = #{id}")
    String getCategoryNameById(Integer id);

    @Select("SELECT * FROM products")
    @ResultMap("ProductResult")
    List<Product> findAll();

    @Update("""
        UPDATE products SET
            product_name = #{productName},
            user_id = #{userId},
            main_category_id = #{mainCategoryId},
            product_price = #{productPrice},
            discount_percent = #{discountPercent},
            product_status = #{productStatus},
            description = #{description},
            location = #{location},
            condition = #{condition}
        WHERE product_id = #{productId}
    """)
    @ResultMap("ProductResult")
    void updateProduct(Product product);

    @Delete("DELETE FROM product_images WHERE product_id = #{productId}")
    @ResultMap("ProductResult")
    void deleteProductImages(Long productId);

    @Delete("DELETE FROM products WHERE product_id = #{productId}")
    @ResultMap("ProductResult")
    void deleteProduct(Long productId);
}
