package com.example.resellkh.repository;

import com.example.resellkh.model.entity.Rating;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RatingRepo {

    @Insert("INSERT INTO ratings (rated_user_id, rating_user_id, score, comment) " +
            "VALUES (#{ratedUserId}, #{ratingUserId}, #{score}, #{comment})")
    @Options(useGeneratedKeys = true, keyProperty = "ratingId", keyColumn = "rating_id")
    void insertRating(Rating rating);

    @Select("SELECT * FROM ratings WHERE rating_id = #{ratingId}")
    @Results(id = "ratingMapper", value = {
            @Result(property = "ratingId", column = "rating_id"),
            @Result(property = "ratedUserId", column = "rated_user_id"),
            @Result(property = "ratingUserId", column = "rating_user_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "comment", column = "comment")
    })
    Rating getRatingById(@Param("ratingId") int ratingId);

    @Select("SELECT * FROM ratings WHERE rated_user_id = #{ratedUserId}")
    @ResultMap("ratingMapper")
    List<Rating> getRatingsByUserId(@Param("ratedUserId") int ratedUserId);
}
