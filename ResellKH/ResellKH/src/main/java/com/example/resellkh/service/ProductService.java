package com.example.resellkh.service;

import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.ProductDraft;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files);
    ProductWithFilesDto updateProduct(Long id, ProductRequest request, MultipartFile[] files);
    List<ProductWithFilesDto> searchByImageUrl(MultipartFile file);
    List<ProductWithFilesDto> findNearbyProducts(double lat, double lng);
    ProductWithFilesDto getProductWithFilesById(Long id);
    List<ProductWithFilesDto> getAllProductsWithFiles();
    List<ProductWithFilesDto> getProductsByUserId(Long userId);
    List<ProductWithFilesDto> getProductsByCategoryId(Integer categoryId);
    List<ProductWithFilesDto> getAllProductsByStatus(String status);

    ProductDraft saveDraftProduct(String productName, Long userId, Long mainCategoryId,
                                         Double productPrice, Double discountPercent, String description,
                                         String location, Double latitude, Double longitude, String condition,
                                         String telegramUrl, MultipartFile[] files);

    // MODIFIED: Added productStatus parameter here
    Object updateDraftProduct(Long draftId, String productName, Long mainCategoryId,
                              Double productPrice, Double discountPercent, String description,
                              String location, Double latitude, Double longitude, String condition,
                              String telegramUrl, String productStatus, MultipartFile[] files);

    ProductDraft getDraftById(Long draftId);

    ProductWithFilesDto updateDraftProductByUserId(Long userId, String productName, Long mainCategoryId,
                                                   Double productPrice, Double discountPercent, String description,
                                                   String location, Double latitude, Double longitude,
                                                   String condition, String telegramUrl, MultipartFile[] files);

    boolean deleteDraftByUserIdAndDraftId(Long userId, Long draftId);

    List<ProductDraft> getDraftsByUser(Long userId);

    ProductDraft publishDraftProduct(Long draftId); // This method is key for publishing

    ProductWithFilesDto deleteProductAndReturnDto(Long id);

    Double getDiscountPercentByProductId(Long productId);
}