package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.service.Impl.ProductServiceImpl;
import com.example.resellkh.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductServiceImpl productServiceImpl;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> uploadProduct(
            @RequestParam("productName") String productName,
            @RequestParam("userId") Long userId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("productPrice") Double productPrice,
            @RequestParam("discountPercent") Double discountPercent,
            @RequestParam("productStatus") String productStatus,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("condition") String condition,
            @RequestPart("files") MultipartFile[] files
    ) {
        if (files == null || files.length < 1 || files.length > 5) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            "You must upload between 1 and 5 image/video files.",
                            null,
                            HttpStatus.BAD_REQUEST.value(),
                            LocalDateTime.now()
                    )
            );
        }

        ProductRequest request = new ProductRequest();
        request.setProductName(productName);
        request.setUserId(userId);
        request.setCategoryName(categoryName);
        request.setProductPrice(productPrice);
        request.setDiscountPercent(discountPercent);
        request.setProductStatus(productStatus);
        request.setDescription(description);
        request.setLocation(location);
        request.setCondition(condition);

        ProductWithFilesDto savedProduct = productService.uploadProductWithCategoryName(request, files);
        savedProduct.setCategoryName(categoryName);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Product uploaded successfully",
                        savedProduct,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> getProductWithFiles(@PathVariable Long id) {
        ProductWithFilesDto dto = productService.getProductWithFilesById(id);

        if (dto == null) {
            return ResponseEntity.ok(
                    ApiResponse.<ProductWithFilesDto>builder()
                            .message("No product found with the given ID.")
                            .payload(null)
                            .status(HttpStatus.OK)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<ProductWithFilesDto>builder()
                        .message("Product with files fetched successfully")
                        .payload(dto)
                        .status(HttpStatus.OK)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> getAll() {
        List<ProductWithFilesDto> list = productService.getAllProductsWithFiles();
        return ResponseEntity.ok(
                ApiResponse.<List<ProductWithFilesDto>>builder()
                        .message("All products with files fetched")
                        .payload(list)
                        .status(HttpStatus.OK)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> updateProduct(
            @PathVariable("id") Long id,
            @RequestParam("productName") String productName,
            @RequestParam("userId") Long userId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam("productPrice") Double productPrice,
            @RequestParam("discountPercent") Double discountPercent,
            @RequestParam("productStatus") String productStatus,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("condition") String condition,
            @RequestPart("files") MultipartFile[] files
    ) {
        if (files == null || files.length < 1 || files.length > 5) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            "You must upload between 1 and 5 image/video files.",
                            null,
                            HttpStatus.BAD_REQUEST.value(),
                            LocalDateTime.now()
                    )
            );
        }

        ProductRequest request = new ProductRequest();
        request.setProductName(productName);
        request.setUserId(userId);
        request.setCategoryName(categoryName);
        request.setProductPrice(productPrice);
        request.setDiscountPercent(discountPercent);
        request.setProductStatus(productStatus);
        request.setDescription(description);
        request.setLocation(location);
        request.setCondition(condition);

        ProductWithFilesDto updatedProduct = productService.updateProduct(id, request, files);
        updatedProduct.setCategoryName(categoryName);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product updated successfully",
                        updatedProduct,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> deleteProduct(@PathVariable Long id) {
        ProductWithFilesDto deletedProduct = productServiceImpl.deleteProductAndReturnDto(id);

        if (deletedProduct == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "No product found to delete.",
                            null,
                            HttpStatus.OK.value(),
                            LocalDateTime.now()
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Product deleted successfully",
                        deletedProduct,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }
}
