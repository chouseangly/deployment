package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.NotificationFavorite;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.model.entity.ProductDraft;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.FavouriteService;
import com.example.resellkh.service.Impl.ProductServiceImpl;
import com.example.resellkh.service.NotificationService;
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
    private final FavouriteService favouriteService;
    private final NotificationService notificationService;
    private final ProductRepo productRepo;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> uploadProductWithCategoryName(
            @RequestParam("productName") String productName,
            @RequestParam("userId") Long userId,
            @RequestParam("mainCategoryId") Long mainCategoryId,
            @RequestParam("productPrice") Double productPrice,
            @RequestParam("discountPercent") Double discountPercent,
            @RequestParam("productStatus") String productStatus,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("condition") String condition,
            @RequestParam("telegramUrl") String telegramUrl,
            @RequestPart("files") MultipartFile[] files
    ) {
        if (!"DRAFT".equalsIgnoreCase(productStatus)) {
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
        }

        ProductRequest request = new ProductRequest(productName, userId, mainCategoryId, productPrice,
                discountPercent, productStatus, description, location, latitude, longitude, condition, telegramUrl);

        ProductWithFilesDto product = productService.uploadProductWithCategoryName(request, files);
        Notification notification = Notification.builder()
                .userId(userId)
                .productId(product.getProductId()) // CRITICAL: The productId is now part of the initial object.
                .title("Add Product")
                .content("You success fully upload product name : "+ productName)
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                .build();
        notificationService.createNotificationWithType(notification);

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
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "updated_at", required = false) LocalDateTime updatedAt,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {

        Double previousDiscount = productService.getDiscountPercentByProductId(id);
        Double newDiscount = discountPercent;
        List<Long> favoriter = favouriteService.getUserIdByProductId(id);

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

        Notification notification1 = Notification.builder()
                .userId(userId)
                .productId(id)
                .title("Update Product")
                .content("You successfully Update Product name : " + productName)
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                .build();
        notificationService.createNotificationWithType(notification1);

        if (previousDiscount < newDiscount) {
            for (Long userIdOfFavoriter : favoriter) {
                String message = "Dear customer, The product you favorited has been updated with a higher discount!. Your favorite product \" " + productName + " \" have new discount with " + newDiscount + " %";
                NotificationFavorite notificationFavorite = notificationService.favoriteNotification(id);

                // Step 3: Build the notification object, including the productId from the start.
                Notification notification = Notification.builder()
                        .userId(userIdOfFavoriter)
                        .productId(id) // CRITICAL: The productId is now part of the initial object.
                        .title("Favourite")
                        .content(message)
                        .iconUrl(notificationFavorite.getProfileImage())
                        .build();

                // Step 4: Create the complete notification in a single, safe database call.
                notificationService.createNotificationWithType(notification);

            }
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Product updated successfully", updatedProduct, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
    // In chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/controller/ProductController.java

    @DeleteMapping("/{productId}/files")
    public ResponseEntity<ApiResponse<String>> deleteProductFile(
            @PathVariable Long productId,
            @RequestParam("fileUrl") String fileUrl) {
        try {
            boolean deleted = productService.deleteProductFileByUrl(productId, fileUrl);
            if (deleted) {
                return ResponseEntity.ok(
                        new ApiResponse<>("File deleted successfully", null, HttpStatus.OK.value(), LocalDateTime.now())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>("File not found or could not be deleted", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now())
            );
        }
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
        Product product = productRepo.findById(id);
        Notification notification = Notification.builder()
                .userId(product.getUserId())
                .productId(product.getProductId())
                .title("Delete Product")
                .content("You success fully delete product name : "+product.getProductName())
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                .build();
        notificationService.createNotificationWithType(notification);
        ProductWithFilesDto deletedProduct = productServiceImpl.deleteProductAndReturnDto(id);
        notificationService.deleteAllNotificationByProductId(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        deletedProduct != null ? "Product deleted successfully" : "No product found to delete.",
                        deletedProduct,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
    @PostMapping(value = "/search-by-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> searchByImageUrl(@RequestParam("file") MultipartFile file) {
        List<ProductWithFilesDto> result = productService.searchByImageUrl(file);
        return ResponseEntity.ok(
                new ApiResponse<>("Similar products found", result, HttpStatus.OK.value(), LocalDateTime.now())
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


    @GetMapping("/filter-by-status")
    public ResponseEntity<ApiResponse<List<ProductWithFilesDto>>> getProductsByStatus(
            @RequestParam("status") String status) {
        List<ProductWithFilesDto> filtered = productService.getAllProductsByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.<List<ProductWithFilesDto>>builder()
                        .message("Products with status: " + status)
                        .payload(filtered)
                        .status(HttpStatus.OK.value())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping(value = "/save-draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDraft>> saveDraft(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "mainCategoryId", required = false) Long mainCategoryId,
            @RequestParam(value = "productPrice", required = false) Double productPrice,
            @RequestParam(value = "discountPercent", required = false) Double discountPercent,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "telegramUrl", required = false) String telegramUrl,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        // productStatus is always "DRAFT" here internally
        ProductDraft draft = productService.saveDraftProduct(
                productName, userId, mainCategoryId, productPrice,
                discountPercent, description, location, latitude, longitude,
                condition, telegramUrl, files
        );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Draft saved successfully",
                        draft,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    // Update draft product info & files
    @PutMapping(value = "/update-draft/{draftId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateDraft(
            @PathVariable Long draftId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "mainCategoryId", required = false) Long mainCategoryId,
            @RequestParam(value = "productPrice", required = false) Double productPrice,
            @RequestParam(value = "discountPercent", required = false) Double discountPercent,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "telegramUrl", required = false) String telegramUrl,
            @RequestParam(value = "productStatus", required = false) String productStatus,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {

        try {
            Object result = productService.updateDraftProduct(
                    draftId, productName, mainCategoryId, productPrice,
                    discountPercent, description, location, latitude, longitude,
                    condition, telegramUrl, productStatus, files
            );

            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>("Draft not found", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
                );
            }

            if (result instanceof ProductWithFilesDto) {
                return ResponseEntity.ok(new ApiResponse<>(
                        "Draft published successfully",
                        result,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(
                        "Draft updated successfully",
                        result,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now())
            );
        }
    }

    // Publish draft: move draft to actual product table
    @PostMapping("/publish-draft/{draftId}")
    public ResponseEntity<ApiResponse<ProductDraft>> publishDraft(@PathVariable Long draftId) {
        ProductDraft publishedProduct = productService.publishDraftProduct(draftId);
        if (publishedProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("Draft product not found or could not be published", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Draft published successfully", publishedProduct, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    // Optionally get all drafts by user
    @GetMapping("/drafts/user/{userId}")
    public ResponseEntity<ApiResponse<List<ProductDraft>>> getDraftsByUser(@PathVariable Long userId) {
        try {
            // 1. Validate userId
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>("Invalid userId provided", null, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now())
                );
            }

            // 2. Get drafts from service
            List<ProductDraft> drafts = productService.getDraftsByUser(userId);

            // 3. Handle null or empty cases with 200 OK and null payload
            if (drafts == null || drafts.isEmpty()) {
                return ResponseEntity.ok(
                        new ApiResponse<>("No drafts found for this user", null, HttpStatus.OK.value(), LocalDateTime.now())
                );
            }

            // 4. Return successful response with drafts
            return ResponseEntity.ok(
                    new ApiResponse<>("Drafts fetched successfully", drafts, HttpStatus.OK.value(), LocalDateTime.now())
            );

        } catch (Exception e) {
            // 5. Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("Error fetching drafts: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now())
            );
        }
    }

    // Optionally get single draft by id
    @GetMapping("/drafts/{draftId}")
    public ResponseEntity<ApiResponse<ProductDraft>> getDraftById(@PathVariable Long draftId) {
        ProductDraft draft = productService.getDraftById(draftId);
        if (draft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("Draft product not found", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Draft fetched successfully", draft, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
    @PutMapping(value = "/update-draft-byuserId", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductWithFilesDto>> updateDraftByUserId(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "mainCategoryId", required = false) Long mainCategoryId,
            @RequestParam(value = "productPrice", required = false) Double productPrice,
            @RequestParam(value = "discountPercent", required = false) Double discountPercent,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "telegramUrl", required = false) String telegramUrl,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        ProductWithFilesDto updatedDraft = productServiceImpl.updateDraftProductByUserId( // Assuming this calls a specific method for userId
                userId, productName, mainCategoryId, productPrice, discountPercent,
                description, location, latitude, longitude, condition, telegramUrl, files
        );

        if (updatedDraft == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("No draft found for the given userId", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Draft updated successfully by userId", updatedDraft, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
    @DeleteMapping("/{draftId}/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteDraftByUserIdAndDraftId(
            @PathVariable Long draftId,
            @PathVariable Long userId) {

        boolean deleted = productService.deleteDraftByUserIdAndDraftId(userId, draftId);

        if (deleted) {
            return ResponseEntity.ok(
                    new ApiResponse<>("Draft deleted successfully", null, HttpStatus.OK.value(), LocalDateTime.now()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>("Draft not found or unauthorized", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now()));
        }
    }

}