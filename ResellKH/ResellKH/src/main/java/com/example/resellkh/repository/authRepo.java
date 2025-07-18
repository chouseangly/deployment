package com.example.resellkh.repository;

import com.example.resellkh.model.entity.Auth;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface authRepo {

    @Select("SELECT * FROM users WHERE email = #{email}")
    @ResultMap("UserResult")
    Optional<Auth> findByEmailOptional(@Param("email") String email);

    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results(id = "UserResult", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "role", column = "role"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "enabled", column = "enabled")
    })
    Auth findByEmail(String email);

    @Insert("""
  INSERT INTO users (user_name, first_name, last_name, email, password, role, enabled, created_at)
  VALUES (#{userName}, #{firstName}, #{lastName}, #{email}, #{password}, #{role}, #{enabled}, #{createdAt})
""")
    void insertUser(Auth auth);


    @Update("UPDATE users SET enabled = true WHERE email = #{email}")
    void enableUserByEmail(String email);

    @Update("UPDATE users SET password = #{password} WHERE email = #{email}")
    void updatePasswordByEmail(@Param("email") String email, @Param("password") String password);

    @Select("SELECT * FROM users")
    @ResultMap("UserResult")
    List<Auth> getAllUser();

}
