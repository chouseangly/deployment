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
    private static final String PYTHON_VECTOR_URL = "https://connectors-backed-dinner-virgin.trycloudflare.com/extract-vector";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Transactional
    @Override
    public ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files) {
        try {
            if (files == null || files.length < 1) {
                throw new IllegalArgumentException("You must upload at least 1 image/video file.");
            }

            MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;

            Product product = new Product();
            BeanUtils.copyProperties(request, product);
            product.setCondition(request.getCondition() != null ? request.getCondition() : "new");
            product.setCreatedAt(LocalDateTime.now());

            if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
            if (product.getProductStatus() == null) product.setProductStatus("");
            if (product.getDescription() == null) product.setDescription("");
            if (product.getLocation() == null) product.setLocation("");
            if (product.getLatitude() == null) product.setLatitude(0.0);
            if (product.getLongitude() == null) product.setLongitude(0.0);
            if (product.getTelegramUrl() == null) product.setTelegramUrl("");

            productRepo.insertProduct(product);

            if (product.getProductId() == null) {
                throw new RuntimeException("Product insertion failed - no ID generated");
            }

            productHistoryService.recordHistory(product.getProductId(), "Product created");

            boolean firstImage = true;
            for (MultipartFile file : limitedFiles) {
                String ipfsUrl = uploadFileToPinata(file);
                String contentType = file.getContentType();

                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(ipfsUrl);
                productFile.setContentType(contentType);
                fileRepo.insertProductFile(productFile);

                if (firstImage && contentType != null && contentType.startsWith("image/")) {
                    saveEmbedding(product.getProductId(), file);
                    firstImage = false;
                }
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
                throw new RuntimeException("Product not found with ID: " + id);
            }

            if (request.getUserId() == null || !request.getUserId().equals(existing.getUserId())) {
                throw new SecurityException("Unauthorized: You do not have permission to edit this product.");
            }

            BeanUtils.copyProperties(request, existing, "userId", "createdAt");
            existing.setProductId(id);
            productRepo.updateProduct(existing);

            productHistoryService.recordHistory(id, "Product details updated.");

            if (files != null && files.length > 0) {
                List<String> existingFileUrls = fileRepo.findUrlsByProductId(id);
                int remainingSlots = 5 - existingFileUrls.size();

                if (remainingSlots > 0) {
                    int filesToAddCount = Math.min(files.length, remainingSlots);
                    MultipartFile[] filesToAdd = Arrays.copyOfRange(files, 0, filesToAddCount);

                    boolean wasFirstEmbeddingDone = productEmbeddingRepo.countByProductId(id) > 0;
                    boolean firstNewImage = true;

                    for (MultipartFile file : filesToAdd) {
                        String ipfsUrl = uploadFileToPinata(file);
                        String contentType = file.getContentType();
                        ProductFile productFile = new ProductFile();
                        productFile.setProductId(id);
                        productFile.setFileUrl(ipfsUrl);
                        productFile.setContentType(contentType);
                        fileRepo.insertProductFile(productFile);

                        if (!wasFirstEmbeddingDone && firstNewImage && contentType != null && contentType.startsWith("image/")) {
                            saveEmbedding(id, file);
                            firstNewImage = false;
                        }
                    }
                    productHistoryService.recordHistory(id, "Added " + filesToAddCount + " new media file(s).");
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
            List<Double> inputVector = callImageVectorAPI(file);
            List<ProductEmbeddingRepo.ProductEmbeddingRecord> allEmbeddings = productEmbeddingRepo.getAllEmbeddings();
            List<ProductWithSimilarity> similarities = new ArrayList<>();

            for (ProductEmbeddingRepo.ProductEmbeddingRecord record : allEmbeddings) {
                try {
                    List<Double> targetVector = OBJECT_MAPPER.readValue(record.getVectorJson(), new TypeReference<>() {});
                    double similarity = computeCosineSimilarity(inputVector, targetVector);
                    similarities.add(new ProductWithSimilarity(record.getProductId(), similarity));
                } catch (IOException e) {
                    System.err.println("Error parsing vector for Product ID " + record.getProductId() + ": " + e.getMessage());
                }
            }

            return similarities.stream()
                    .filter(sim -> sim.similarity > 0.6)
                    .sorted(Comparator.comparingDouble(ProductWithSimilarity::similarity).reversed())
                    .map(sim -> getProductWithFilesById(sim.productId()))
                    .filter(Objects::nonNull)
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to search by image: " + e.getMessage(), e);
        }
    }

    private void saveEmbedding(Long productId, MultipartFile imageFile) {
        try {
            List<Double> vector = callImageVectorAPI(imageFile);
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

    private List<Double> callImageVectorAPI(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    PYTHON_VECTOR_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() == null || !(response.getBody().get("vector") instanceof List<?> rawList)) {
                throw new RuntimeException("Invalid or empty vector format received from Python service.");
            }

            return rawList.stream()
                    .map(val -> Double.valueOf(String.valueOf(val)))
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            throw new RuntimeException("Error from Python service: " + e.getStatusCode() + " - " + errorBody, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("A general error occurred while calling the Python vector service: " + e.getMessage(), e);
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
            productHistoryService.recordHistory(productId, "Product deleted");
            fileRepo.deleteFilesByProductId(productId);
            productEmbeddingRepo.deleteByProductId(productId);
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
        draft.setProductStatus("DRAFT");
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

        ProductDraft existing = draftProductFileRepo.findById(draftId);
        if (existing == null) {
            return null;
        }

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

        if (productStatus != null) {
            existing.setProductStatus(productStatus.trim().toUpperCase());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        if ("ON SALE".equals(existing.getProductStatus())) {
            Product product = new Product();
            BeanUtils.copyProperties(existing, product);
            product.setCreatedAt(LocalDateTime.now());

            if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
            if (product.getDescription() == null) product.setDescription("");
            if (product.getLocation() == null) product.setLocation("");
            if (product.getLatitude() == null) product.setLatitude(0.0);
            if (product.getLongitude() == null) product.setLongitude(0.0);
            if (product.getTelegramUrl() == null) product.setTelegramUrl("");
            if (product.getCondition() == null) product.setCondition("NEW");

            productRepo.insertProduct(product);

            List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draftId);
            boolean firstImage = true;
            for(ProductDraftFile draftFile : draftFiles){
                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(draftFile.getUrl());
                fileRepo.insertProductFile(productFile);
                if(firstImage){
                    // Embedding on publish is complex since the file is not available
                    firstImage = false;
                }
            }

            draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
            draftProductFileRepo.deleteDraftProduct(draftId);

            return mapToProductDto(product);
        }

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

        dto.setFileUrls(fileRepo.findByProductId(product.getProductId())
                .stream()
                .map(ProductFile::getFileUrl)
                .collect(Collectors.toList()));

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

        List<ProductDraftFile> files = draftProductFileRepo.findByDraftId(draftId);
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
        List<ProductDraft> drafts = draftProductFileRepo.findByUserId(userId);
        if (drafts == null || drafts.isEmpty()) {
            return null;
        }

        ProductDraft draft = drafts.get(0);

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

        if (files != null && files.length > 0) {
            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId());
            saveFilesForDraft(draft.getDraftId(), files);
        }

        Product existingProduct = productRepo.findByDraftId(draft.getDraftId());

        if (existingProduct == null) {
            Product newProduct = new Product();
            BeanUtils.copyProperties(draft, newProduct);
            newProduct.setProductStatus("On sale");
            newProduct.setUpdatedAt(LocalDateTime.now());
            productRepo.insertProduct(newProduct);

            List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draft.getDraftId());
            for (ProductDraftFile draftFile : draftFiles) {
                ProductFile productFile = new ProductFile();
                productFile.setProductId(newProduct.getProductId());
                productFile.setFileUrl(draftFile.getUrl());
                fileRepo.insertProductFile(productFile);
            }

            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId());
            draftProductFileRepo.deleteDraftProduct(draft.getDraftId());

            return getProductWithFilesById(newProduct.getProductId());
        } else {
            BeanUtils.copyProperties(draft, existingProduct, "draftId", "createdAt");
            existingProduct.setProductStatus("On sale");
            existingProduct.setUpdatedAt(LocalDateTime.now());
            productRepo.updateProduct(existingProduct);

            if (files != null && files.length > 0) {
                fileRepo.deleteFilesByProductId(existingProduct.getProductId());
                saveFilesForProduct(existingProduct.getProductId(), files);
            } else {
                List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draft.getDraftId());
                fileRepo.deleteFilesByProductId(existingProduct.getProductId());
                for (ProductDraftFile draftFile : draftFiles) {
                    ProductFile productFile = new ProductFile();
                    productFile.setProductId(existingProduct.getProductId());
                    productFile.setFileUrl(draftFile.getUrl());
                    fileRepo.insertProductFile(productFile);
                }
            }
            draftProductFileRepo.deleteDraftFilesByDraftId(draft.getDraftId());
            draftProductFileRepo.deleteDraftProduct(draft.getDraftId());

            return getProductWithFilesById(existingProduct.getProductId());
        }
    }

    private void saveFilesForProduct(Long productId, MultipartFile[] files) {
        if (files == null || files.length == 0) return;

        MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
        for (MultipartFile file : limitedFiles) {
            String url = uploadFileToPinata(file);
            ProductFile productFile = new ProductFile();
            productFile.setProductId(productId);
            productFile.setFileUrl(url);
            fileRepo.insertProductFile(productFile);
        }
    }


    @Transactional
    @Override
    public boolean deleteDraftByUserIdAndDraftId(Long userId, Long draftId) {
        ProductDraft draft = draftProductFileRepo.findById(draftId);
        if (draft == null || !draft.getUserId().equals(userId)) {
            return false;
        }
        draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
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

        Product product = new Product();
        BeanUtils.copyProperties(draft, product);
        product.setProductId(null);
        product.setProductStatus("On sale");
        product.setUpdatedAt(LocalDateTime.now());

        if (product.getDiscountPercent() == null) product.setDiscountPercent(0.0);
        if (product.getDescription() == null) product.setDescription("");
        if (product.getLocation() == null) product.setLocation("");
        if (product.getLatitude() == null) product.setLatitude(0.0);
        if (product.getLongitude() == null) product.setLongitude(0.0);
        if (product.getTelegramUrl() == null) product.setTelegramUrl("");
        if (product.getCondition() == null) product.setCondition("new");

        productRepo.insertProduct(product);

        productHistoryService.recordHistory(product.getProductId(), "Product published from draft");

        List<ProductDraftFile> draftFiles = draftProductFileRepo.findByDraftId(draftId);
        if (draftFiles != null && !draftFiles.isEmpty()) {
            boolean firstImage = true;
            for (ProductDraftFile draftFile : draftFiles) {
                ProductFile productFile = new ProductFile();
                productFile.setProductId(product.getProductId());
                productFile.setFileUrl(draftFile.getUrl());
                fileRepo.insertProductFile(productFile);
                if(firstImage){
                    // Embedding on publish from draft is complex and unreliable
                    firstImage = false;
                }
            }
        }

        draftProductFileRepo.deleteDraftFilesByDraftId(draftId);
        draftProductFileRepo.deleteDraftProduct(draftId);

        return getProductWithFilesById(product.getProductId());
    }

    private void saveFilesForDraft(Long draftId, MultipartFile[] files) {
        if (files == null || files.length == 0) return;

        MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;
        for (MultipartFile file : limitedFiles) {
            String url = uploadFileToPinata(file);

            ProductDraftFile draftFile = new ProductDraftFile();
            draftFile.setDraftId(draftId);
            draftFile.setUrl(url);
            draftProductFileRepo.insertDraftProductFile(draftFile);
        }
    }

    @Override
    public Double getDiscountPercentByProductId(Long productId) {
        return productRepo.getDiscountPercent(productId);
    }

    @Override
    public boolean deleteProductFileByUrl(Long productId, String fileUrl) {
        try {
            int affectedRows = fileRepo.deleteFileByProductIdAndUrl(productId, fileUrl);
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Error deleting file from database: " + e.getMessage());
            throw new RuntimeException("Failed to delete file for product ID: " + productId, e);
        }
    }
}
