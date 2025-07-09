package com.example.resellkh.repository;

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

    @Select("SELECT n.id, n.user_id, n.title, n.content, n.is_read, n.created_at, n.updated_at, nt.type_id, nt.type_name, nt.icon_url " +
            "FROM notifications n LEFT JOIN notification_type nt ON n.type_id = nt.type_id WHERE n.user_id = #{userId} ORDER BY n.created_at DESC ")
    List<Notification> getAllNotificationsByUserId(@Param("userId") int userId);

    @Update("UPDATE notifications SET is_read = true WHERE user_id = #{userId} AND id = #{id}")
    void markNotificationAsRead(@Param("userId") int userId, @Param("id") int id);

    @Insert("INSERT INTO notifications (user_id, title, content, type_id) " +
            "VALUES (#{userId}, #{title}, #{content}, #{typeId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createNotificationWithType(Notification notification);

}
