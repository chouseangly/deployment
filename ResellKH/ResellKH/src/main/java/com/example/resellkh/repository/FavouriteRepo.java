package com.example.resellkh.repository;

import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavouriteRepo {

    @Select("INSERT INTO favourites (user_id, product_id, created_at) VALUES (#{userId}, #{productId}, #{createdAt}) returning *")
    @Results(id = "favouriteMapper", value = {
            @Result(property = "favouriteId",column = "favourite_id"),
            @Result(property = "userId",column = "user_id"),
            @Result(property = "productId",column = "product_id"),
            @Result(property = "createdAt",column = "created_at")
    })
    Favourite addFavourite(FavouriteRequest favouriteRequest);
    @Select("SELECT * FROM favourites WHERE user_id = #{userId} AND product_id = #{productId}")
    @ResultMap("favouriteMapper")
    List<Favourite> getFavourites(@Param("userId") Integer userId, @Param("productId") Integer productId);


    @Select("DELETE FROM favourites WHERE user_id = #{userId} AND product_id = #{productId} RETURNING *")
    @ResultMap("favouriteMapper")
    Favourite removeFavourite(@Param("userId") Integer userId, @Param("productId") Integer productId);

    @Select("SELECT * FROM favourites WHERE user_id = #{userId}")
    @ResultMap("favouriteMapper")
    List<Favourite> getFavouritesByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM favourites WHERE user_id = #{userId} AND product_id = #{productId}")
    boolean isFavourite(@Param("userId") Integer userId, @Param("productId") Integer productId);
}

