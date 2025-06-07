package com.example.resellkh.repository;

import com.example.resellkh.model.entity.ContactInfo;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContactInfoRepo {

    @Insert("""
        INSERT INTO contact_info (user_id, telegram_url) 
        VALUES (#{userId}, #{telegramUrl})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addContactInfo(ContactInfo contactInfo);
    @Select("""
        SELECT * FROM contact_info
""")
    List<ContactInfo> getAllContactInfo();

    @Select("""
    SELECT * FROM contact_info WHERE user_id = #{userId}
""")
    List<ContactInfo> getContactByUserId(Long userId);
}
