package com.example.resellkh.repository;

import com.example.resellkh.model.dto.NotificationFavorite;
import com.example.resellkh.model.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationRepo {

    @Insert("INSERT INTO notifications (user_id, content) VALUES (#{userId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createNotification(Notification notification);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> getNotificationsByUserId(@Param("userId") int userId);

    @Select("SELECT n.id, n.user_id, n.title, n.content, n.is_read, n.created_at, n.updated_at, n.icon_url " +
            "FROM notifications n  WHERE n.user_id = #{userId} ORDER BY n.created_at DESC ")
    List<Notification> getAllNotificationsByUserId(@Param("userId") int userId);

    @Update("UPDATE notifications SET is_read = true WHERE user_id = #{userId} AND id = #{id}")
    void markNotificationAsRead(@Param("userId") int userId, @Param("id") int id);

    @Insert("INSERT INTO notifications (user_id, title, content, icon_url) " +
            "VALUES (#{userId}, #{title}, #{content}, #{iconUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createNotificationWithType(Notification notification);

    @Select(("SELECT " +
            "    p.description ," +
            "    up.profile_image " +
            "FROM products p " +
            "LEFT JOIN user_profile up ON p.user_id = up.user_id\n " +
            "WHERE p.product_id = #{productId}"))
    NotificationFavorite getFavoriteNotification(@Param("productId") Long productId);

    @Update("UPDATE notifications SET product_id = #{productId} WHERE id = #{id}")
    int insertProductId(@Param("productId") int productId, @Param("id") int id);

    @Select("SELECT * FROM notifications WHERE id = #{id}")
    Notification findById(@Param("id") int id);

    @Select("SELECT product_id FROM notifications WHERE id = #{id}")
    Integer getProductIdByNoId(@Param("id") int id); // Changed from int to Integer

}