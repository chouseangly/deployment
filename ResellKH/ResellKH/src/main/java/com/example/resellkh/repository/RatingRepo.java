package com.example.resellkh.repository;

import com.example.resellkh.model.dto.RatingWithUserDTO;
import com.example.resellkh.model.entity.Rating;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RatingRepo {
    @Select("""
    SELECT 
        r.rating_id, r.rated_user_id, r.rating_user_id, 
        r.score, r.comment, r.created_at,
        up.user_name AS reviewer_name,
        up.profile_image AS reviewer_avatar
    FROM ratings r
    LEFT JOIN user_profile up ON r.rating_user_id = up.user_id
    ORDER BY r.created_at DESC
    """)
    @Results({
            @Result(property = "ratingId", column = "rating_id"),
            @Result(property = "ratedUserId", column = "rated_user_id"),
            @Result(property = "ratingUserId", column = "rating_user_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "reviewerName", column = "reviewer_name"),
            @Result(property = "reviewerAvatar", column = "reviewer_avatar")
    })
    List<RatingWithUserDTO> getAllRating();


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
            @Result(property = "comment", column = "comment"),
            @Result(property = "createdAt", column = "created_at")
    })
    Rating getRatingById(@Param("ratingId") int ratingId);

    @Select("""
        SELECT 
            r.rating_id, r.rated_user_id, r.rating_user_id, 
            r.score, r.comment, r.created_at,
            up.user_name AS reviewer_name,
            up.profile_image AS reviewer_avatar
        FROM ratings r
        JOIN user_profile up ON r.rating_user_id = up.user_id
        WHERE r.rated_user_id = #{ratedUserId}
        ORDER BY r.created_at DESC
        """)
    @Results({
            @Result(property = "ratingId", column = "rating_id"),
            @Result(property = "ratedUserId", column = "rated_user_id"),
            @Result(property = "ratingUserId", column = "rating_user_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "reviewerName", column = "reviewer_name"),
            @Result(property = "reviewerAvatar", column = "reviewer_avatar")
    })
    List<RatingWithUserDTO> getRatingsWithUserDetails(@Param("ratedUserId") int ratedUserId);

    @Select("""
    SELECT 
        r.rating_id, r.rated_user_id, r.rating_user_id, 
        r.score, r.comment, r.created_at,
        up.user_name AS reviewer_name,
        up.profile_image AS reviewer_avatar
    FROM ratings r
    JOIN user_profile up ON r.rating_user_id = up.user_id
    WHERE r.rating_id = #{ratingId}
    """)
    @Results({
            @Result(property = "ratingId", column = "rating_id"),
            @Result(property = "ratedUserId", column = "rated_user_id"),
            @Result(property = "ratingUserId", column = "rating_user_id"),
            @Result(property = "score", column = "score"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "reviewerName", column = "reviewer_name"),
            @Result(property = "reviewerAvatar", column = "reviewer_avatar")
    })
    RatingWithUserDTO getRatingWithDetailsById(@Param("ratingId") int ratingId);
    @Select("""
    SELECT ROUND(AVG(score), 1)
    FROM ratings
    WHERE rated_user_id = #{userId}
""")
    Double getAverageScoreByUserId(int userId);

    @Select("""
    SELECT COUNT(*)
    FROM ratings
    WHERE rated_user_id = #{userId}
""")
    int getReviewCountByUserId(int userId);



}