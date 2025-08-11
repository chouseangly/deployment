package com.example.resellkh.repository;

import com.example.resellkh.model.dto.ProductFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductFileRepo {

    @Insert("INSERT INTO product_images(product_id, url, content_type) VALUES (#{productId}, #{fileUrl}, #{contentType})")
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "fileUrl", column = "url"),
            @Result(property = "contentType", column = "content_type")
    })
    void insertProductFile(ProductFile file);

    @Select("SELECT product_id, url, content_type FROM product_images WHERE product_id = #{productId}")
    @Results({
            @Result(property = "productId", column = "product_id"),
            @Result(property = "fileUrl", column = "url"),
            @Result(property = "contentType", column = "content_type")
    })
    List<ProductFile> findByProductId(Long productId);
    @Select("SELECT url FROM product_images WHERE product_id = #{productId}")
    List<String> findUrlsByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM product_images WHERE product_id = #{productId}")
    void deleteFilesByProductId(Long productId);

    @Delete("DELETE FROM product_images WHERE product_id = #{productId} AND url = #{fileUrl}")
    int deleteFileByProductIdAndUrl(@Param("productId") Long productId, @Param("fileUrl") String fileUrl);;
}
