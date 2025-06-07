package com.example.resellkh.repository;

import com.example.resellkh.model.dto.UserProfileRequest;
import com.example.resellkh.model.entity.UserProfile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserProfileRepo {

    @Insert("""
        INSERT INTO user_profile (user_id, gender, phone_number, profile_image, cover_image, birthday, address)
        VALUES (#{userId}, #{gender}, #{phoneNumber}, #{profileImage}, #{coverImage}, #{birthday}, #{address})
    """)
    void createUserProfile(UserProfileRequest request);

    @Update("""
        UPDATE user_profile
        SET gender = #{gender},
            phone_number = #{phoneNumber},
            profile_image = #{profileImage},
            cover_image = #{coverImage},
            birthday = #{birthday},
            address = #{address}
        WHERE user_id = #{userId}
    """)
    void updateUserProfile(UserProfileRequest request);

    @Select("""
        SELECT profile_id, user_id, gender, phone_number, profile_image, cover_image, birthday, address
        FROM user_profile
        WHERE user_id = #{userId}
    """)
    @Results({
            @Result(property = "profileId", column = "profile_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "birthday", column = "birthday"),
            @Result(property = "address", column = "address")
    })
    UserProfile getProfileByUserId(Long userId);

    @Delete("DELETE FROM user_profile WHERE user_id = #{userId}")
    int deleteProfile(Long userId);

    @Select("""
    SELECT * FROM user_profile;
""")
    @Results({
            @Result(property = "profileId", column = "profile_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "birthday", column = "birthday"),
            @Result(property = "address", column = "address")
    })
    UserProfile getUserProfiles();
}
