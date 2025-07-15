package com.example.resellkh.repository;

import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductRepo {

    @Insert("""
        INSERT INTO products (
            product_name, user_id, main_category_id, product_price, discount_percent,
            product_status, description, location, "condition", latitude, longitude,
            created_at, telegram_url
        ) VALUES (
            #{productName}, #{userId}, #{mainCategoryId}, #{productPrice}, #{discountPercent},
            #{productStatus}, #{description}, #{location}, #{condition}, #{latitude},
            #{longitude}, #{createdAt}, #{telegramUrl}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "productId", keyColumn = "product_id")
    void insertProduct(Product product);

    //Additional Query Update
    @Update("""
        UPDATE products
        SET
            product_name = #{product.productName},
            main_category_id = #{product.mainCategoryId},
            product_price = #{product.productPrice},
            discount_percent = #{product.discountPercent},
            product_status = #{product.productStatus},
            description = #{product.description},
            location = #{product.location},
            "condition" = #{product.condition},
            latitude = #{product.latitude},
            longitude = #{product.longitude},
            telegram_url = #{product.telegramUrl},
            updated_at = NOW()
        WHERE product_id = #{product.productId} AND user_id = #{product.userId}
    """)
    void editProduct(@Param("product") Product product);


    @Select("SELECT main_category_id FROM main_category WHERE name = #{name}")
    Integer getCategoryIdByName(String name);

    @Results(id = "ProductResult", value = {
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "imageUrls", column = "url")
    })
    @Select("SELECT * FROM products WHERE product_id = #{id}")
    Product findById(Long id);

    @Select("""
    SELECT DISTINCT p.*
    FROM products p 
    LEFT JOIN product_images pi ON p.product_id = pi.product_id 
    WHERE p.user_id = #{userId}
    """)
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "telegramUrl", column = "telegram_url"),
            @Result(property = "fileUrls", column = "product_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<ProductWithFilesDto> findByUserId(@Param("userId") Long userId);

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
            condition = #{condition},
            telegram_url = #{telegramUrl},
            latitude = #{latitude},
            longitude = #{longitude},
            updated_at = NOW()
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

    @Select("""
    SELECT p.*, 
           STRING_AGG(pi.url, ',') AS file_urls,
           (6371 * acos(
               cos(radians(#{userLat})) *
               cos(radians(p.latitude)) *
               cos(radians(p.longitude) - radians(#{userLng})) +
               sin(radians(#{userLat})) *
               sin(radians(p.latitude))
           )) AS distance
    FROM products p
    LEFT JOIN product_images pi ON p.product_id = pi.product_id
    GROUP BY p.product_id
    HAVING (6371 * acos(
               cos(radians(#{userLat})) *
               cos(radians(p.latitude)) *
               cos(radians(p.longitude) - radians(#{userLng})) +
               sin(radians(#{userLat})) *
               sin(radians(p.latitude))
           )) <= 5
    ORDER BY distance
    """)
    List<Product> findNearbyProducts(
            @Param("userLat") double userLat,
            @Param("userLng") double userLng
    );

    @Select("""
    SELECT p.*, 
           string_agg(pi.url, ',') AS file_urls
    FROM products p
    LEFT JOIN product_images pi ON p.product_id = pi.product_id
    WHERE main_category_id = #{mainCategoryId}
    GROUP BY p.product_id
    ORDER BY p.product_id
    """)
    @ResultMap("ProductResult")
    List<Product> findByCategoryId(@Param("mainCategoryId") Integer mainCategoryId);

    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "categoryName", column = "name"), // from main_category table alias
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "telegramUrl", column = "telegram_url"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "fileUrls", column = "product_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    @Select("""
    SELECT p.*, c.name
    FROM products p
    LEFT JOIN main_category c ON p.main_category_id = c.main_category_id
    WHERE p.product_id = #{productId}
""")
    ProductWithFilesDto findProductDtoById(@Param("productId") Long productId);


    @Select("""
        SELECT p.product_id AS productId,
               p.name AS productName,
               p.user_id AS userId,
               p.main_category_id AS mainCategoryId,
               c.name AS categoryName,
               p.price AS productPrice,
               p.discount_percent AS discountPercent,
               p.status AS productStatus,
    """)

    List<Product> getProductsInCartByUserId(Long userId);
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    void clearCartByUserId(Long userId);
    @Select("""
    SELECT p.*, c.name AS category_name
    FROM products p
    LEFT JOIN main_category c ON p.main_category_id = c.main_category_id
    WHERE p.user_id = #{userId} AND p.product_status = #{status}
""")
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "categoryName", column = "category_name"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "telegramUrl", column = "telegram_url"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "fileUrls", column = "product_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<ProductWithFilesDto> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
// In ProductRepo interface, add these methods:

    @Update("""
    UPDATE products 
    SET product_status = #{status}, 
        updated_at = NOW() 
    WHERE product_id = #{productId} AND user_id = #{userId}
""")
    int updateProductStatus(@Param("productId") Long productId,
                            @Param("userId") Long userId,
                            @Param("status") String status);

    @Select("""
    SELECT p.*, c.name AS category_name
    FROM products p
    LEFT JOIN main_category c ON p.main_category_id = c.main_category_id
    WHERE p.user_id = #{userId} AND LOWER(p.product_status) = 'draft'
""")
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "categoryName", column = "category_name"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "telegramUrl", column = "telegram_url"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "fileUrls", column = "product_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<ProductWithFilesDto> findDraftsByUserId(@Param("userId") Long userId);

    @Select("""
    SELECT p.*, c.name AS category_name
    FROM products p
    LEFT JOIN main_category c ON p.main_category_id = c.main_category_id
    WHERE LOWER(p.product_status) = LOWER(#{status})
""")
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "productName", column = "product_name"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "mainCategoryId", column = "main_category_id"),
            @Result(property = "categoryName", column = "category_name"),
            @Result(property = "productPrice", column = "product_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "productStatus", column = "product_status"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "telegramUrl", column = "telegram_url"),
            @Result(property = "condition", column = "condition"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "fileUrls", column = "product_id",
                    many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<ProductWithFilesDto> findAllByStatus(@Param("status") String status);

    @Select("SELECT product_status FROM products WHERE product_id = #{productId} AND user_id = #{userId}")
    String getProductStatusForUser(@Param("productId") Long productId, @Param("userId") Long userId);
    @Select("SELECT * FROM products WHERE product_id = #{productId}")

    Product findByDraftId(Long draftId);
}
