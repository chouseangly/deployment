package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.ProductFile;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.model.entity.ProductDraft;
import com.example.resellkh.model.entity.ProductDraftFile;
import com.example.resellkh.repository.DraftProductFileRepo;
import com.example.resellkh.repository.ProductEmbeddingRepo;
import com.example.resellkh.repository.ProductFileRepo;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.ProductHistoryService;
import com.example.resellkh.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final ProductFileRepo fileRepo;
    private final ProductHistoryService productHistoryService;
    private final ProductEmbeddingRepo productEmbeddingRepo;
    private final RestTemplate restTemplate;
    private final DraftProductFileRepo draftProductFileRepo;
    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";
    private static final String PYTHON_VECTOR_URL = "https://technological-publishers-gif-school.trycloudflare.com/extract-vector";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Transactional
    @Override
    public ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files) {
        try {
            // Validate files
            if (files == null || files.length < 1) {
                throw new IllegalArgumentException("You must upload at least 1 image/video file.");
            }

            // Limit files to 5
            MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;

            // Create product and set properties
            Product product = new Product();
            // Copy non-reserved properties
            BeanUtils.copyProperties(request, product);
            // Explicitly set reserved keyword field and ensure it's not null
            product.setCondition(request.getCondition() != null ? request.getCondition() : "new");
            product.setCreatedAt(LocalDateTime.now());

            // Set defaults for nullable fields
            if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
            if (product.getProductStatus() == null) product.setProductStatus("");
            if (product.getDescription() == null) product.setDescription("");
            if (product.getLocation() == null) product.setLocation("");
            if (product.getLatitude() == null) product.setLatitude(0.0);
            if (product.getLongitude() == null) product.setLongitude(0.0);
            if (product.getTelegramUrl() == null) product.setTelegramUrl("");

            // Insert product
            productRepo.insertProduct(product);

            // Verify insertion
            if (product.getProductId() == null) {
                throw new RuntimeException("Product insertion failed - no ID generated");
            }

            productHistoryService.recordHistory(product.getProductId().intValue(), "Product created");

            // Process files
            List<String> fileUrls = new ArrayList<>();
            for (MultipartFile file : limitedFiles) {
                String ipfsUrl = uploadFileToPinata(file);
                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(ipfsUrl);
                fileRepo.insertProductFile(productFile);
                fileUrls.add(ipfsUrl);
            }

            // Save embedding if files exist
            if (!fileUrls.isEmpty()) {
                saveEmbedding(product.getProductId(), fileUrls.get(0));
            }

            return getProductWithFilesById(product.getProductId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload product: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ProductWithFilesDto updateProduct(Long id, ProductRequest request, MultipartFile[] files) {
        try {
            Product existing = productRepo.findById(id);
            if (existing == null) {
                throw new RuntimeException("Product not found");
            }

            // Update only non-null fields
            if (request.getProductName() != null) existing.setProductName(request.getProductName());
            if (request.getUserId() != null) existing.setUserId(request.getUserId());
            if (request.getMainCategoryId() != null) existing.setMainCategoryId(request.getMainCategoryId());
            if (request.getProductPrice() != null) existing.setProductPrice(request.getProductPrice());
            if (request.getDiscountPercent() != null) existing.setDiscountPercent(request.getDiscountPercent());
            if (request.getProductStatus() != null) existing.setProductStatus(request.getProductStatus());
            if (request.getDescription() != null) existing.setDescription(request.getDescription());
            if (request.getLocation() != null) existing.setLocation(request.getLocation());
            if (request.getLatitude() != null) existing.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) existing.setLongitude(request.getLongitude());
            if (request.getCondition() != null) existing.setCondition(request.getCondition());
            if (request.getTelegramUrl() != null) existing.setTelegramUrl(request.getTelegramUrl());

            productRepo.updateProduct(existing);
            productHistoryService.recordHistory(id.intValue(), "Product updated");

            // Process files if provided
            if (files != null && files.length > 0) {
                fileRepo.deleteFilesByProductId(id);

                MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
                List<String> fileUrls = new ArrayList<>();
                for (MultipartFile file : limitedFiles) {
                    String ipfsUrl = uploadFileToPinata(file);
                    ProductFile productFile = new ProductFile();
                    productFile.setProductId(id);
                    productFile.setFileUrl(ipfsUrl);
                    fileRepo.insertProductFile(productFile);
                    fileUrls.add(ipfsUrl);
                }

                if (!fileUrls.isEmpty()) {
                    saveEmbedding(id, fileUrls.get(0));
                }
            }

            return getProductWithFilesById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductWithFilesDto> searchByImageUrl(MultipartFile file) {
        try {
            String ipfsUrl = uploadFileToPinata(file);
            List<Double> inputVector = callImageVectorAPI(ipfsUrl);

            List<ProductEmbeddingRepo.ProductEmbeddingRecord> all = productEmbeddingRepo.getAllEmbeddings();
            List<ProductWithSimilarity> similarities = new ArrayList<>();

            for (ProductEmbeddingRepo.ProductEmbeddingRecord record : all) {
                try {
                    List<Double> targetVector = OBJECT_MAPPER.readValue(record.getVectorJson(), new TypeReference<>() {});
                    double similarity = computeCosineSimilarity(inputVector, targetVector);
                    similarities.add(new ProductWithSimilarity(record.getProductId(), similarity));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to parse vector JSON", e);
                }
            }

            return similarities.stream()
                    .filter(sim -> sim.similarity > 0.8)
                    .sorted(Comparator.comparingDouble(ProductWithSimilarity::similarity).reversed())
                    .map(sim -> getProductWithFilesById(sim.productId))
                    .filter(Objects::nonNull)
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search by image: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductWithFilesDto> findNearbyProducts(double lat, double lng) {
        try {
            List<Product> all = productRepo.findNearbyProducts(lat, lng);
            if (all.isEmpty()) return Collections.emptyList();
            return all.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find nearby products: " + e.getMessage(), e);
        }
    }



    private record ProductWithSimilarity(Long productId, double similarity) {}

    private void saveEmbedding(Long productId, String imageUrl) {
        try {
            List<Double> vector = callImageVectorAPI(imageUrl);
            String vectorJson = new ObjectMapper().writeValueAsString(vector);
            int count = productEmbeddingRepo.countByProductId(productId);

            if (count > 0) {
                productEmbeddingRepo.updateEmbedding(productId, vectorJson);
            } else {
                productEmbeddingRepo.insertEmbedding(productId, vectorJson);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to save embedding: " + e.getMessage(), e);
        }
    }

    // Replace the existing callImageVectorAPI method in your ProductServiceImpl.java
// with this new version.

    // Replace the existing callImageVectorAPI method in your ProductServiceImpl.java
// with this new, correct version.

    private List<Double> callImageVectorAPI(String imageUrl) {
        try {
            // STEP 1: Create the correct JSON body {"url": "..."} that the Python service expects.
            // We use a Map to represent the JSON object.
            Map<String, String> requestBody = Map.of("url", imageUrl);

            // STEP 2: Set the headers to indicate we are sending JSON data.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // STEP 3: Create the HTTP request entity with the JSON body and headers.
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // STEP 4: Make the POST request and get the response.
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    PYTHON_VECTOR_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() == null || !(response.getBody().get("vector") instanceof List<?> rawList)) {
                throw new RuntimeException("Invalid or empty vector format received from Python service.");
            }

            // Convert the result to a List of Doubles.
            return rawList.stream()
                    .map(val -> Double.valueOf(String.valueOf(val)))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python vector service for URL " + imageUrl + ": " + e.getMessage(), e);
        }
    }
    private double computeCosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public ProductWithFilesDto getProductWithFilesById(Long id) {
        try {
            Product product = productRepo.findById(id);
            return (product == null) ? null : mapToDto(product);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductWithFilesDto> getAllProductsWithFiles() {
        try {
            return productRepo.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all products: " + e.getMessage(), e);
        }
    }


    public ProductWithFilesDto deleteProductAndReturnDto(Long productId) {
        try {
            Product product = productRepo.findById(productId);
            if (product == null) return null;

            ProductWithFilesDto dto = mapToDto(product);
            productHistoryService.recordHistory(productId.intValue(), "Product deleted");
            fileRepo.deleteFilesByProductId(productId);
            productRepo.deleteProduct(productId);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    private ProductWithFilesDto mapToDto(Product product) {
        ProductWithFilesDto dto = new ProductWithFilesDto();
        BeanUtils.copyProperties(product, dto);
        dto.setFileUrls(fileRepo.findByProductId(product.getProductId())
                .stream().map(ProductFile::getFileUrl).collect(Collectors.toList()));

        String categoryName = productRepo.getCategoryNameById(product.getMainCategoryId().intValue());
        dto.setCategoryName(categoryName != null ? categoryName : "Unknown");
        return dto;
    }

    private String uploadFileToPinata(MultipartFile file) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(PINATA_URL);
            post.setHeader("pinata_api_key", pinataApiKey);
            post.setHeader("pinata_secret_api_key", pinataSecretApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), ContentType.DEFAULT_BINARY,
                    UUID.randomUUID() + "_" + file.getOriginalFilename());

            org.apache.http.HttpEntity entity = builder.build();
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                return "https://gateway.pinata.cloud/ipfs/" + new ObjectMapper().readTree(json).get("IpfsHash").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Pinata", e);
        }
    }

    @Override
    public List<ProductWithFilesDto> getProductsByUserId(Long userId) {
        try {
            List<ProductWithFilesDto> products = productRepo.findByUserId(userId);

            products.forEach(product -> {
                String categoryName = productRepo.getCategoryNameById(
                        product.getMainCategoryId() != null ?
                                product.getMainCategoryId().intValue() : 0);
                product.setCategoryName(categoryName != null ? categoryName : "Unknown");
            });

            return products;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get products by user ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductWithFilesDto> getProductsByCategoryId(Integer categoryId) {
        try {
            List<Product> product = productRepo.findByCategoryId(categoryId);
            return product.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get products by category ID: " + e.getMessage(), e);
        }
    }



    @Override
    public List<ProductWithFilesDto> getAllProductsByStatus(String status) {
        return productRepo.findAllByStatus(status);
    }

    @Transactional
    @Override
    public ProductDraft saveDraftProduct(String productName, Long userId, Long mainCategoryId,
                                         Double productPrice, Double discountPercent, String description,
                                         String location, Double latitude, Double longitude, String condition,
                                         String telegramUrl, MultipartFile[] files) {
        ProductDraft draft = new ProductDraft();
        draft.setProductName(productName);
        draft.setUserId(userId);
        draft.setMainCategoryId(mainCategoryId);
        draft.setProductPrice(productPrice);
        draft.setDiscountPercent(discountPercent != null ? discountPercent : 0.0);
        draft.setProductStatus("DRAFT");  // consistent uppercase draft status
        draft.setDescription(description != null ? description : "");
        draft.setLocation(location != null ? location : "");
        draft.setLatitude(latitude != null ? latitude : 0.0);
        draft.setLongitude(longitude != null ? longitude : 0.0);
        draft.setCondition(condition != null ? condition : "new");
        draft.setTelegramUrl(telegramUrl != null ? telegramUrl : "");
        draft.setCreatedAt(LocalDateTime.now());
        draft.setUpdatedAt(LocalDateTime.now());

        draftProductFileRepo.insertDraftProduct(draft);

        saveFilesForDraft(draft.getDraftId(), files);

        return getDraftById(draft.getDraftId());
    }

    @Transactional
    @Override
    public Object updateDraftProduct(Long draftId, String productName, Long mainCategoryId,
                                     Double productPrice, Double discountPercent, String description,
                                     String location, Double latitude, Double longitude, String condition,
                                     String telegramUrl, String productStatus, MultipartFile[] files) {

        // 1. Find existing draft
        ProductDraft existing = draftProductFileRepo.findById(draftId);
        if (existing == null) {
            return null;
        }

        // 2. Update all fields
        if (productName != null) existing.setProductName(productName);
        if (mainCategoryId != null) existing.setMainCategoryId(mainCategoryId);
        if (productPrice != null) existing.setProductPrice(productPrice);
        if (discountPercent != null) existing.setDiscountPercent(discountPercent);
        if (description != null) existing.setDescription(description);
        if (location != null) existing.setLocation(location);
        if (latitude != null) existing.setLatitude(latitude);
        if (longitude != null) existing.setLongitude(longitude);
        if (condition != null) existing.setCondition(condition);
        if (telegramUrl != null) existing.setTelegramUrl(telegramUrl);

        // 3. Handle status update
        if (productStatus != null) {
            existing.setProductStatus(productStatus.trim().toUpperCase());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        // 4. Check if we should publish
        if ("ON SALE".equals(existing.getProductStatus())) {
            // Create new product
            Product product = new Product();
            BeanUtils.copyProperties(existing, product);
            product.setCreatedAt(LocalDateTime.now());

            // Set defaults
            if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
            if (product.getDescription() == null) product.setDescription("");
            if (product.getLocation() == null) product.setLocation("");
            if (product.getLatitude() == null) product.setLatitude(0.0);
            if (product.getLongitude() == null) product.setLongitude(0.0);
            if (product.getTelegramUrl() == null) product.setTelegramUrl("");
            if (product.getCondition() == null) product.setCondition("NEW");

            // Insert product
            productRepo.insertProduct(product);

            // Transfer files
            List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draftId);
            draftFiles.forEach(draftFile -> {
                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(draftFile.getUrl());
                fileRepo.insertProductFile(productFile);
            });

            // Save embedding
            if (!draftFiles.isEmpty()) {
                saveEmbedding(product.getProductId(), draftFiles.get(0).getUrl());
            }

            // Delete draft
            draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
            draftProductFileRepo.deleteDraftProduct(draftId);

            // Return the new product DTO
            return mapToProductDto(product);
        }

        // 5. Regular draft update
        draftProductFileRepo.updateDraftProduct(existing);

        if (files != null && files.length > 0) {
            draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
            saveFilesForDraft(draftId, files);
        }

        return getDraftById(draftId);
    }

    private ProductWithFilesDto mapToProductDto(Product product) {
        ProductWithFilesDto dto = new ProductWithFilesDto();
        BeanUtils.copyProperties(product, dto);

        // Set file URLs
        dto.setFileUrls(fileRepo.findByProductId(product.getProductId())
                .stream()
                .map(ProductFile::getFileUrl)
                .collect(Collectors.toList()));

        // Set category name
        String categoryName = productRepo.getCategoryNameById(
                product.getMainCategoryId() != null ?
                        product.getMainCategoryId().intValue() : 0);
        dto.setCategoryName(categoryName != null ? categoryName : "Unknown");

        return dto;
    }
    @Override
    public ProductDraft getDraftById(Long draftId) {
        ProductDraft draft = draftProductFileRepo.findById(draftId);
        if (draft == null) return null;

        ProductDraft dto = new ProductDraft();
        BeanUtils.copyProperties(draft, dto);

        List<ProductDraftFile> files = draftProductFileRepo.findByDraftId(draftId);  // fixed method name: findByDraftId
        dto.setFileUrls(files.stream().map(ProductDraftFile::getUrl).collect(Collectors.toList()));

        String categoryName = productRepo.getCategoryNameById(
                draft.getMainCategoryId() != null ? draft.getMainCategoryId().intValue() : 0);
        dto.setCategoryName(categoryName != null ? categoryName : "Unknown");

        return dto;
    }

    @Transactional
    @Override
    public ProductWithFilesDto updateDraftProductByUserId(Long userId, String productName, Long mainCategoryId,
                                                          Double productPrice, Double discountPercent, String description,
                                                          String location, Double latitude, Double longitude,
                                                          String condition, String telegramUrl, MultipartFile[] files) {
        // Find drafts by userId
        List<ProductDraft> drafts = draftProductFileRepo.findByUserId(userId);
        if (drafts == null || drafts.isEmpty()) {
            return null;  // no draft found
        }

        ProductDraft draft = drafts.get(0); // update the first draft (you can adjust logic)

        // Update draft fields if not null
        if (productName != null) draft.setProductName(productName);
        if (mainCategoryId != null) draft.setMainCategoryId(mainCategoryId);
        if (productPrice != null) draft.setProductPrice(productPrice);
        if (discountPercent != null) draft.setDiscountPercent(discountPercent);
        if (description != null) draft.setDescription(description);
        if (location != null) draft.setLocation(location);
        if (latitude != null) draft.setLatitude(latitude);
        if (longitude != null) draft.setLongitude(longitude);
        if (condition != null) draft.setCondition(condition);
        if (telegramUrl != null) draft.setTelegramUrl(telegramUrl);

        draft.setUpdatedAt(LocalDateTime.now());
        draftProductFileRepo.updateDraftProduct(draft);

        // Update draft files if any
        if (files != null && files.length > 0) {
            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId()); // Also fixed here for consistency
            MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
            for (MultipartFile file : limitedFiles) {
                String url = uploadFileToPinata(file);
                ProductDraftFile draftFile = new ProductDraftFile();
                draftFile.setDraftId(draft.getDraftId());
                draftFile.setUrl(url);
                draftProductFileRepo.insertDraftProductFile(draftFile);
            }
        }

        // Check if product exists linked to this draft (implement findByDraftId in ProductRepo)
        Product existingProduct = productRepo.findByDraftId(draft.getDraftId());

        if (existingProduct == null) {
            // Create new product from draft
            Product newProduct = new Product();
            newProduct.setProductName(draft.getProductName());
            newProduct.setUserId(draft.getUserId());
            newProduct.setMainCategoryId(draft.getMainCategoryId());
            newProduct.setProductPrice(draft.getProductPrice());
            newProduct.setDiscountPercent(draft.getDiscountPercent());
            newProduct.setProductStatus("On sale");  // Set status to "On sale"
            newProduct.setDescription(draft.getDescription());
            newProduct.setLocation(draft.getLocation());
            newProduct.setLatitude(draft.getLatitude());
            newProduct.setLongitude(draft.getLongitude());
            newProduct.setCondition(draft.getCondition());
            newProduct.setTelegramUrl(draft.getTelegramUrl());
            newProduct.setCreatedAt(draft.getCreatedAt());
            newProduct.setUpdatedAt(LocalDateTime.now());

            productRepo.insertProduct(newProduct);

            // Insert product files from draft files
            List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draft.getDraftId());
            for (ProductDraftFile draftFile : draftFiles) {
                ProductFile productFile = new ProductFile();
                productFile.setProductId(newProduct.getProductId());
                productFile.setFileUrl(draftFile.getUrl());
                fileRepo.insertProductFile(productFile);
            }

            // Delete draft files and draft itself after publishing
            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId()); // Also fixed here
            draftProductFileRepo.deleteDraftProduct(draft.getDraftId());

            return getProductWithFilesById(newProduct.getProductId());

        } else {
            // Update existing product from draft info
            existingProduct.setProductName(draft.getProductName());
            existingProduct.setMainCategoryId(draft.getMainCategoryId());
            existingProduct.setProductPrice(draft.getProductPrice());
            existingProduct.setDiscountPercent(draft.getDiscountPercent());
            existingProduct.setProductStatus("On sale");  // Set status to "On sale"
            existingProduct.setDescription(draft.getDescription());
            existingProduct.setLocation(draft.getLocation());
            existingProduct.setLatitude(draft.getLatitude());
            existingProduct.setLongitude(draft.getLongitude());
            existingProduct.setCondition(draft.getCondition());
            existingProduct.setTelegramUrl(draft.getTelegramUrl());
            existingProduct.setUpdatedAt(draft.getUpdatedAt());

            productRepo.updateProduct(existingProduct);

            // Update product files
            if (files != null && files.length > 0) {
                fileRepo.deleteFilesByProductId(existingProduct.getProductId());
                MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
                for (MultipartFile file : limitedFiles) {
                    String url = uploadFileToPinata(file);
                    ProductFile productFile = new ProductFile();
                    productFile.setProductId(existingProduct.getProductId());
                    productFile.setFileUrl(url);
                    fileRepo.insertProductFile(productFile);
                }
            } else {
                // Sync draft files to product files if no new files uploaded
                List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draft.getDraftId());
                fileRepo.deleteFilesByProductId(existingProduct.getProductId());
                for (ProductDraftFile draftFile : draftFiles) {
                    ProductFile productFile = new ProductFile();
                    productFile.setProductId(existingProduct.getProductId());
                    productFile.setFileUrl(draftFile.getUrl());
                    fileRepo.insertProductFile(productFile);
                }
            }

            // Delete draft files and draft itself after publishing
            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId()); // Also fixed here
            draftProductFileRepo.deleteDraftProduct(draft.getDraftId());

            return getProductWithFilesById(existingProduct.getProductId());
        }
    }

    @Transactional
    @Override
    public boolean deleteDraftByUserIdAndDraftId(Long userId, Long draftId) {
        ProductDraft draft = draftProductFileRepo.findById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            return false;  // Not found or not authorized
        }

        // Delete draft files first
        draftProductFileRepo.deleteDraftFilesByDraftId(draftId);

        // Delete the draft itself
        draftProductFileRepo.deleteDraftByDraftIdAndUserId(draftId, userId);

        return true;
    }

    @Transactional
    @Override
    public List<ProductDraft> getDraftsByUser(Long userId) {
        List<ProductDraft> drafts = draftProductFileRepo.findByUserId(userId);
        if (drafts == null) return Collections.emptyList();

        List<ProductDraft> dtos = new ArrayList<>();
        for (ProductDraft draft : drafts) {
            ProductDraft dto = new ProductDraft();
            BeanUtils.copyProperties(draft, dto);

            List<ProductDraftFile> files = draftProductFileRepo.findByDraftId(draft.getDraftId());
            dto.setFileUrls(files.stream().map(ProductDraftFile::getUrl).collect(Collectors.toList()));

            String categoryName = productRepo.getCategoryNameById(
                    draft.getMainCategoryId() != null ? draft.getMainCategoryId().intValue() : 0);
            dto.setCategoryName(categoryName != null ? categoryName : "Unknown");

            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    @Override
    public ProductDraft publishDraftProduct(Long draftId) {
        ProductDraft draft = draftProductFileRepo.findById(draftId);
        if (draft == null) return null;

        // Create a new Product from the draft
        Product product = new Product();
        BeanUtils.copyProperties(draft, product); // Copy common properties
        product.setProductId(null); // Ensure new ID is generated for the new product
        product.setProductStatus("On sale"); // Set final status for published product
        product.setCreatedAt(draft.getCreatedAt()); // Keep original draft creation time
        product.setUpdatedAt(LocalDateTime.now()); // Set updated time to now (publication time)

        // Handle nullable fields (ensure default values if source ProductDraft has nulls)
        if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
        if (product.getDescription() == null) product.setDescription("");
        if (product.getLocation() == null) product.setLocation("");
        if (product.getLatitude() == null) product.setLatitude(0.0);
        if (product.getLongitude() == null) product.setLongitude(0.0);
        if (product.getTelegramUrl() == null) product.setTelegramUrl("");
        if (product.getCondition() == null) product.setCondition("new");

        productRepo.insertProduct(product); // Insert product, new productId will be set

        // Record product history
        productHistoryService.recordHistory(product.getProductId().intValue(), "Product published from draft");

        // Copy files from draft_images to product_images
        List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draftId);
        List<String> publishedFileUrls = new ArrayList<>();
        if (draftFiles != null && !draftFiles.isEmpty()) {
            for (ProductDraftFile draftFile : draftFiles) {
                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(draftFile.getUrl()); // Reuse existing IPFS URL
                fileRepo.insertProductFile(productFile);
                publishedFileUrls.add(draftFile.getUrl());
            }
            // Save embedding for the first image of the newly published product
            saveEmbedding(product.getProductId(), publishedFileUrls.get(0));
        }

        // Delete draft files and the draft itself
        draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
        draftProductFileRepo.deleteDraftProduct(draftId);

        // Return the newly published product DTO
        return getProductWithFilesById(product.getProductId());
    }

    // Helper to save files for a draft
    private void saveFilesForDraft(Long draftId, MultipartFile[] files) {
        if (files == null || files.length == 0) return;

        MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
        for (MultipartFile file : limitedFiles) {
            String url = uploadFileToPinata(file); // Upload to Pinata

            ProductDraftFile draftFile = new ProductDraftFile();
            draftFile.setDraftId(draftId);
            draftFile.setUrl(url);
            draftProductFileRepo.insertDraftProductFile(draftFile);
        }
    }
}