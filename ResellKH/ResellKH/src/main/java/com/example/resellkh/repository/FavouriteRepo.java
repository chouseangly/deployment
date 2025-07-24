package com.example.resellkh.repository;

import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavouriteRepo {

    @Select("INSERT INTO favourites (user_id, product_id, created_at) VALUES (#{userId}, #{productId}, #{createdAt}) returning *")
    @Results(id = "favouriteMapper", value = {
            @Result(property = "favouriteId", column = "favourite_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    Favourite addFavourite(FavouriteRequest favouriteRequest);

    @Select("SELECT * FROM favourites WHERE user_id = #{userId} AND product_id = #{productId}")
    @ResultMap("favouriteMapper")
    List<Favourite> getFavourites(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("DELETE FROM favourites WHERE user_id = #{userId} AND product_id = #{productId} RETURNING *")
    @ResultMap("favouriteMapper")
    Favourite removeFavourite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) > 0 FROM favourites WHERE user_id = #{userId} AND product_id = #{productId}")
    boolean isFavourite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("""
        SELECT
            f.favourite_id, f.user_id, f.product_id AS f_product_id, f.created_at,
            p.product_id, p.product_name, p.product_price, p.discount_percent, p.product_status,
            p.description, p.location, p.condition, p.latitude, p.longitude, p.main_category_id,
            mc.name AS category_name, p.created_at AS product_created_at, p.telegram_url, p.user_id AS product_user_id
        FROM favourites f
        JOIN products p ON f.product_id = p.product_id
        LEFT JOIN main_category mc ON p.main_category_id = mc.main_category_id
        WHERE f.user_id = #{userId} ORDER BY f.created_at DESC
    """)
    @Results({
            @Result(property = "favouriteId", column = "favourite_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "f_product_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "product.productId", column = "product_id"),
            @Result(property = "product.productName", column = "product_name"),
            @Result(property = "product.productPrice", column = "product_price"),
            @Result(property = "product.discountPercent", column = "discount_percent"),
            @Result(property = "product.productStatus", column = "product_status"),
            @Result(property = "product.description", column = "description"),
            @Result(property = "product.location", column = "location"),
            @Result(property = "product.condition", column = "condition"),
            @Result(property = "product.latitude", column = "latitude"),
            @Result(property = "product.longitude", column = "longitude"),
            @Result(property = "product.mainCategoryId", column = "main_category_id"),
            @Result(property = "product.categoryName", column = "category_name"),
            @Result(property = "product.createdAt", column = "product_created_at"),
            @Result(property = "product.telegramUrl", column = "telegram_url"),
            @Result(property = "product.userId", column = "product_user_id"),
            @Result(property = "product.fileUrls", column = "product_id", many = @Many(select = "com.example.resellkh.repository.ProductFileRepo.findUrlsByProductId"))
    })
    List<Favourite> getFavouritesWithProductByUserId(Long userId);

    @Select("SELECT user_id FROM favourites WHERE product_id = #{productId}")
    List<Long> findUserIdsByProductId(@Param("productId") Long productId);
}