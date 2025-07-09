package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.repository.ProductEmbeddingRepo;
import com.example.resellkh.service.Impl.ProductServiceImpl;
import com.example.resellkh.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductServiceImpl productServiceImpl;

    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> uploadProductWithCategoryName(
            @RequestParam("productName") String productName,
            @RequestParam("userId") Long userId,
            @RequestParam("mainCategoryId") Long mainCategoryId,
            @RequestParam("productPrice") Double productPrice,
            @RequestParam("discountPercent") Double discountPercent,
            @RequestParam("productStatus") String productStatus,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("latitude" ) Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("condition") String condition,
            @RequestParam("telegramUrl") String telegramUrl,
            @RequestPart("files") MultipartFile[] files
    ) {
        if (files == null || files.length < 1) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            "You must upload at least 1 image or video file.",
                            null,
                            HttpStatus.BAD_REQUEST.value(),
                            LocalDateTime.now()
                    )
            );
        }

       /* double[] latLng = getLatLngFromLocation(location);
        if (latLng == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            "Invalid location. Could not determine coordinates.",
                            null,
                            HttpStatus.BAD_REQUEST.value(),
                            LocalDateTime.now()
                    )
            );
        }*/

        ProductRequest request = new ProductRequest(productName, userId, mainCategoryId, productPrice,
                discountPercent, productStatus, description, location, latitude, longitude, condition, telegramUrl);

        ProductWithFilesDto product = productService.uploadProductWithCategoryName(request, files);
    //    product.setCategoryName(categoryName);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product uploaded successfully (only first 5 files stored)",
                        product,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @PutMapping(value = "/updateproduct/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> updateProduct(
            @PathVariable("id") Long id,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "mainCategoryId", required = false) Long mainCategoryId,
            @RequestParam(value = "productPrice", required = false) Double productPrice,
            @RequestParam(value = "discountPercent", required = false) Double discountPercent,
            @RequestParam(value = "productStatus", required = false) String productStatus,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "telegramUrl", required = false) String telegramUrl,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        // Convert location to lat/lng if provided
        Double latitude = null;
        Double longitude = null;

        if (location != null && !location.isEmpty()) {
            double[] latLng = getLatLngFromLocation(location);
            if (latLng == null) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("Invalid location. Could not determine coordinates.", null, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now())
                );
            }
            latitude = latLng[0];
            longitude = latLng[1];
        }

        ProductRequest request = new ProductRequest(
                productName,
                userId,
                mainCategoryId,
                productPrice,
                discountPercent,
                productStatus,
                description,
                location,
                latitude,
                longitude,
                condition,
                telegramUrl
        );

        ProductWithFilesDto updatedProduct = productService.updateProduct(id, request, files);

        if (updatedProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("Product not found", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Product updated successfully", updatedProduct, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }


    private double[] getLatLngFromLocation(String location) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + location + "&format=json&limit=1";
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders() {{
                        set("User-Agent", "SpringBoot-App");
                    }}),
                    List.class
            );

            if (!response.getBody().isEmpty()) {
                Map data = (Map) response.getBody().get(0);
                double lat = Double.parseDouble(data.get("lat").toString());
                double lon = Double.parseDouble(data.get("lon").toString());
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> getProductWithFiles(@PathVariable Long id) {
        ProductWithFilesDto dto = productService.getProductWithFilesById(id);

        return ResponseEntity.ok(
                ApiResponse.<ProductWithFilesDto>builder()
                        .message(dto != null ? "Product with files fetched successfully" : "No product found with the given ID.")
                        .payload(dto)
                        .status(HttpStatus.OK)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    @GetMapping("/getproductbyuserid/{userId}")
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> getProductByUserId(
            @PathVariable Long userId) {

        List<ProductWithFilesDto> products = productService.getProductsByUserId(userId);
        String message = products.isEmpty() ?
                "No products found for user ID: " + userId :
                "Products fetched successfully";

        return ResponseEntity.ok(
                ApiResponse.<List<ProductWithFilesDto>>builder()
                        .message(message)
                        .payload(products)
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> deleteProduct(@PathVariable Long id) {
        ProductWithFilesDto deletedProduct = productServiceImpl.deleteProductAndReturnDto(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        deletedProduct != null ? "Product deleted successfully" : "No product found to delete.",
                        deletedProduct,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
    @PostMapping(value = "/search-by-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> searchByImageUrlz(@RequestParam("file") MultipartFile file) {
        List<ProductWithFilesDto> result = productService.searchByImageUrl(file);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Similar products found",
                        result,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
    @GetMapping("/nearby/{lat}/{lng}")
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> getNearbyProducts(
            @PathVariable double lat,
            @PathVariable double lng
            ) {
        List<ProductWithFilesDto> products = productService.findNearbyProducts(lat, lng); // radius = 5 km
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Nearby products fetched successfully",
                        products,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/getbycategoryid/{mainCategoryId}")
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> getProductByCategoryId(@PathVariable Integer mainCategoryId) {
        List<ProductWithFilesDto> products = productService.getProductsByCategoryId(mainCategoryId);
        return ResponseEntity.ok(
                ApiResponse.<List<ProductWithFilesDto>>builder()
                        .message("All products by categoryId")
                        .payload(products)
                        .status(HttpStatus.OK.value())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }




}


