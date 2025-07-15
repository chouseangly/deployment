// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/repository/SellerRepo.java
package com.example.resellkh.repository;

import com.example.resellkh.model.entity.Seller;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SellerRepo {

    @Insert("""
        INSERT INTO seller (user_id, business_name, business_type, business_address, business_description, expected_revenue, bank_name, bank_account_name, bank_account_number, created_at)
        VALUES (#{userId}, #{businessName}, #{businessType}, #{businessAddress}, #{businessDescription}, #{expectedRevenue}, #{bankName}, #{bankAccountName}, #{bankAccountNumber}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "sellerId", keyColumn = "seller_id")
    int insertSeller(Seller seller);

    @Update("""
        UPDATE seller SET
            business_name = #{businessName},
            business_type = #{businessType},
            business_address = #{businessAddress},
            business_description = #{businessDescription},
            expected_revenue = #{expectedRevenue},
            bank_name = #{bankName},
            bank_account_name = #{bankAccountName},
            bank_account_number = #{bankAccountNumber}
        WHERE seller_id = #{sellerId}
    """)
    int updateSeller(Seller seller);

    @Select("SELECT * FROM seller WHERE seller_id = #{sellerId}")
    @ResultMap("sellerResultMap")
    Seller getSellerBySellerId(Long sellerId);

    @Select("SELECT * FROM seller WHERE user_id = #{userId}")
    @Results(id = "sellerResultMap", value = {
            @Result(property = "sellerId", column = "seller_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "businessName", column = "business_name"),
            @Result(property = "businessType", column = "business_type"),
            @Result(property = "businessAddress", column = "business_address"),
            @Result(property = "businessDescription", column = "business_description"),
            @Result(property = "expectedRevenue", column = "expected_revenue"),
            @Result(property = "bankName", column = "bank_name"),
            @Result(property = "bankAccountName", column = "bank_account_name"),
            @Result(property = "bankAccountNumber", column = "bank_account_number"),
            @Result(property = "createdAt", column = "created_at")
    })
    Seller findByUserId(Long userId);
}