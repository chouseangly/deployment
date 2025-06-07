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
}
