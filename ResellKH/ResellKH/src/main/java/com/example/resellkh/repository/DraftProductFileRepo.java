package com.example.resellkh.repository;

import com.example.resellkh.model.entity.ProductDraft;
import com.example.resellkh.model.entity.ProductDraftFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DraftProductFileRepo {

    // ========== DRAFT PRODUCT ==========

    @Insert("""
        INSERT INTO product_drafts (
            product_name, user_id, main_category_id, product_price,
            discount_percent, product_status, description, location,
            latitude, longitude, condition, telegram_url, created_at, updated_at
        ) VALUES (
            #{productName}, #{userId}, #{mainCategoryId}, #{productPrice},
            #{discountPercent}, #{productStatus}, #{description}, #{location},
            #{latitude}, #{longitude}, #{condition}, #{telegramUrl}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "draftId", keyColumn = "draft_id")
    void insertDraftProduct(ProductDraft draft);

    @Select("SELECT * FROM product_drafts WHERE draft_id = #{draftId}")
    ProductDraft findById(Long draftId);

    @Update("""
        UPDATE product_drafts SET
            product_name = #{productName},
            main_category_id = #{mainCategoryId},
            product_price = #{productPrice},
            discount_percent = #{discountPercent},
            product_status = #{productStatus},
            description = #{description},
            location = #{location},
            latitude = #{latitude},
            longitude = #{longitude},
            condition = #{condition},
            telegram_url = #{telegramUrl},
            updated_at = NOW()
        WHERE draft_id = #{draftId}
    """)
    void updateDraftProduct(ProductDraft draft);

    @Select("SELECT * FROM product_drafts WHERE user_id = #{userId}")
    List<ProductDraft> findByUserId(Long userId);

    @Delete("DELETE FROM product_drafts WHERE draft_id = #{draftId}")
    void deleteDraftProduct(Long draftId);

    // Delete draft by draftId and userId to ensure ownership
    @Delete("DELETE FROM product_drafts WHERE draft_id = #{draftId} AND user_id = #{userId}")
    void deleteDraftByDraftIdAndUserId(@Param("draftId") Long draftId, @Param("userId") Long userId);

    // ========== DRAFT IMAGES ==========

    @Insert("INSERT INTO product_draft_images (draft_id, url) VALUES (#{draftId}, #{url})")
    void insertDraftProductFile(ProductDraftFile file);

    @Delete("DELETE FROM product_draft_images WHERE draft_id = #{draftId}")
    void deleteDraftFilesByDraftId(Long draftId);

    @Select("SELECT * FROM product_draft_images WHERE draft_id = #{draftId}")
    List<ProductDraftFile> findByDraftId(Long draftId);


}
