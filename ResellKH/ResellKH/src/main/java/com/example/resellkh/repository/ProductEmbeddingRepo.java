package com.example.resellkh.repository;

import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface ProductEmbeddingRepo {
    @Insert("INSERT INTO product_embeddings(product_id, vector) VALUES(#{productId}, CAST(#{vectorJson} AS jsonb))")
    void insertEmbedding(@Param("productId") Long productId, @Param("vectorJson") String vectorJson);

    @Select("SELECT product_id, vector FROM product_embeddings")
    @Results({
            @Result(property="productId", column="product_id"),
            @Result(property="vectorJson", column="vector")
    })
    List<ProductEmbeddingRecord> getAllEmbeddings();

    @Update("UPDATE product_embeddings SET vector = CAST(#{vectorJson} AS jsonb) WHERE product_id = #{productId}")
    int updateEmbedding(@Param("productId") Long productId, @Param("vectorJson") String vectorJson);


    @Select("SELECT COUNT(*) FROM product_embeddings WHERE product_id = #{productId}")
    int countByProductId(@Param("productId") Long productId);

    class ProductEmbeddingRecord {
        private Long productId;
        private String vectorJson;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getVectorJson() { return vectorJson; }
        public void setVectorJson(String vectorJson) { this.vectorJson = vectorJson; }
    }
}

