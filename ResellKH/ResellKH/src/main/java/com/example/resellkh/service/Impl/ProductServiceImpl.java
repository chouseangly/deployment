package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.ProductFile;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.repository.ProductEmbeddingRepo;
import com.example.resellkh.repository.ProductFileRepo;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.ProductHistoryService;
import com.example.resellkh.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";
    private static final String PYTHON_VECTOR_URL ="https://regions-gray-colony-tsunami.trycloudflare.com/extract-vector";

    @Override
    public ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files) {
        if (files == null || files.length < 1) {
            throw new IllegalArgumentException("You must upload at least 1 image/video file.");
        }

        MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;

        Integer categoryId = productRepo.getCategoryIdByName(request.getCategoryName());
        if (categoryId == null) {
            throw new RuntimeException("Invalid category name: " + request.getCategoryName());
        }

        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setMainCategoryId(Long.valueOf(categoryId));
        product.setCreatedAt(LocalDateTime.now());
        productRepo.insertProduct(product);
        productHistoryService.recordHistory(product.getProductId().intValue(), "Product created");

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : limitedFiles) {
            String ipfsUrl = uploadFileToPinata(file);
            ProductFile productFile = new ProductFile();
            productFile.setProductId(product.getProductId());
            productFile.setFileUrl(ipfsUrl);
            fileRepo.insertProductFile(productFile);
            fileUrls.add(ipfsUrl);
        }

        if (!fileUrls.isEmpty()) {
            saveEmbedding(product.getProductId(), fileUrls.get(0));
        }

        return getProductWithFilesById(product.getProductId());
    }

    @Override
    public ProductWithFilesDto updateProduct(Long id, ProductRequest request, MultipartFile[] files) {
        if (files == null || files.length < 1) {
            throw new IllegalArgumentException("You must upload at least 1 image/video file.");
        }

        MultipartFile[] limitedFiles = files.length > 5 ? Arrays.copyOfRange(files, 0, 5) : files;

        Integer categoryId = productRepo.getCategoryIdByName(request.getCategoryName());
        if (categoryId == null) {
            throw new RuntimeException("Invalid category name: " + request.getCategoryName());
        }

        Product existing = productRepo.findById(id);
        if (existing == null) return null;

        Product updatedProduct = new Product();
        BeanUtils.copyProperties(request, updatedProduct);
        updatedProduct.setProductId(id);
        updatedProduct.setMainCategoryId(Long.valueOf(categoryId));
        updatedProduct.setCreatedAt(existing.getCreatedAt());
        updatedProduct.setLatitude(request.getLatitude());
        updatedProduct.setLongitude(request.getLongitude());
        productRepo.updateProduct(updatedProduct);
        productHistoryService.recordHistory(id.intValue(), "Product updated");

        fileRepo.deleteFilesByProductId(id);

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

        return getProductWithFilesById(id);
    }

    @Override
    public List<ProductWithFilesDto> searchByImageUrl(MultipartFile file) {
        // Step 1: Upload file to Pinata
        String ipfsUrl = uploadFileToPinata(file);

        // Step 2: Call vector API with full IPFS URL
        List<Double> inputVector = callImageVectorAPI(ipfsUrl);

        // Step 3: Compare with existing vectors
        List<ProductEmbeddingRepo.ProductEmbeddingRecord> all = productEmbeddingRepo.getAllEmbeddings();
        List<ProductWithSimilarity> similarities = new ArrayList<>();

        for (ProductEmbeddingRepo.ProductEmbeddingRecord record : all) {
            try {
                List<Double> targetVector = new ObjectMapper().readValue(record.getVectorJson(), List.class);
                double similarity = computeCosineSimilarity(inputVector, targetVector);
                similarities.add(new ProductWithSimilarity(record.getProductId(), similarity));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return similarities.stream()
                .filter(sim -> sim.similarity > 0.7)
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                .map(sim -> getProductWithFilesById(sim.productId))
                .filter(Objects::nonNull)
                .limit(10)
                .toList();
    }



    @Override
    public List<ProductWithFilesDto> findNearbyProducts(double lat, double lng, double radiusKm) {
        List<Product> all = productRepo.findAll();
        List<ProductWithFilesDto> nearby = new ArrayList<>();

        for (Product product : all) {
            if (product.getLatitude() == 0 || product.getLongitude() == 0) continue;

            double distance = haversine(lat, lng, product.getLatitude(), product.getLongitude());
            if (distance <= radiusKm) {
                nearby.add(mapToDto(product));
            }
        }

        return nearby;
    }
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    private record ProductWithSimilarity(Long productId, double similarity) {}


    private void saveEmbedding(Long productId, String imageUrl) {
        List<Double> vector = callImageVectorAPI(imageUrl);
        try {
            String vectorJson = new ObjectMapper().writeValueAsString(vector);
            productEmbeddingRepo.insertEmbedding(productId, vectorJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save embedding: " + e.getMessage(), e);
        }
    }


    private List<Double> callImageVectorAPI(String imageUrl) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> requestBody = Map.of("url", imageUrl);
            String jsonBody = mapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    PYTHON_VECTOR_URL, HttpMethod.POST, request,
                    new ParameterizedTypeReference<>() {}
            );

            Object rawVector = response.getBody().get("vector");
            if (!(rawVector instanceof List<?> rawList)) {
                throw new RuntimeException("Invalid vector format");
            }

            return rawList.stream()
                    .map(val -> ((Number) val).doubleValue())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python vector service: " + e.getMessage(), e);
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
        Product product = productRepo.findById(id);
        return (product == null) ? null : mapToDto(product);
    }

    @Override
    public List<ProductWithFilesDto> getAllProductsWithFiles() {
        return productRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProductWithFilesDto deleteProductAndReturnDto(Long productId) {
        Product product = productRepo.findById(productId);
        if (product == null) return null;

        ProductWithFilesDto dto = mapToDto(product);
        productHistoryService.recordHistory(productId.intValue(), "Product deleted");
        fileRepo.deleteFilesByProductId(productId);
        productRepo.deleteProduct(productId);
        return dto;
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
}
