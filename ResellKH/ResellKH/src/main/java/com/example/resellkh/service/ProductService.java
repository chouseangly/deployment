package com.example.resellkh.service;

import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {
    ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files);
    ProductWithFilesDto getProductWithFilesById(Long id);
    List<ProductWithFilesDto> getAllProductsWithFiles();

    ProductWithFilesDto updateProduct(Long id, ProductRequest request, MultipartFile[] files);

    List<ProductWithFilesDto> searchByImageUrl(MultipartFile file);

    List<ProductWithFilesDto> findNearbyProducts(double lat, double lng);
    List<ProductWithFilesDto> getProductsByUserId(Long userId);
    List<ProductWithFilesDto> getProductsByCategoryId(Integer categoryId);

}
