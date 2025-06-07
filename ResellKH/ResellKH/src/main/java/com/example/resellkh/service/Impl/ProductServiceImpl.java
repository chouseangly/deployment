package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.ProductFile;
import com.example.resellkh.model.dto.ProductRequest;
import com.example.resellkh.model.dto.ProductWithFilesDto;
import com.example.resellkh.model.entity.Product;
import com.example.resellkh.repository.ProductFileRepo;
import com.example.resellkh.repository.ProductRepo;
import com.example.resellkh.service.ProductHistoryService;
import com.example.resellkh.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final ProductFileRepo fileRepo;
    private final ProductHistoryService productHistoryService;

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    @Override
    public ProductWithFilesDto uploadProductWithCategoryName(ProductRequest request, MultipartFile[] files) {
        if (files == null || files.length < 1 || files.length > 5) {
            throw new IllegalArgumentException("You must upload between 1 and 5 files.");
        }

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

        for (MultipartFile file : files) {
            String ipfsUrl = uploadFileToPinata(file);
            ProductFile productFile = new ProductFile();
            productFile.setProductId(product.getProductId());
            productFile.setFileUrl(ipfsUrl);
            fileRepo.insertProductFile(productFile);
        }

        return getProductWithFilesById(product.getProductId());
    }

    @Override
    public ProductWithFilesDto updateProduct(Long id, ProductRequest request, MultipartFile[] files) {
        if (files == null || files.length < 1 || files.length > 5) {
            throw new IllegalArgumentException("You must upload between 1 and 5 files.");
        }

        Integer categoryId = productRepo.getCategoryIdByName(request.getCategoryName());
        if (categoryId == null) {
            throw new RuntimeException("Invalid category name: " + request.getCategoryName());
        }

        Product existing = productRepo.findById(id);
        if (existing == null) {
            return null;
        }

        Product updatedProduct = new Product();
        BeanUtils.copyProperties(request, updatedProduct);
        updatedProduct.setProductId(id);
        updatedProduct.setMainCategoryId(Long.valueOf(categoryId));
        updatedProduct.setCreatedAt(existing.getCreatedAt());

        productRepo.updateProduct(updatedProduct);
        productHistoryService.recordHistory(id.intValue(), "Product updated");

        fileRepo.deleteFilesByProductId(id);

        for (MultipartFile file : files) {
            String ipfsUrl = uploadFileToPinata(file);
            ProductFile productFile = new ProductFile();
            productFile.setProductId(id);
            productFile.setFileUrl(ipfsUrl);
            fileRepo.insertProductFile(productFile);
        }

        return getProductWithFilesById(id);
    }




    @Override
    public ProductWithFilesDto getProductWithFilesById(Long id) {
        Product product = productRepo.findById(id);
        if (product == null) {
            return null;
        }
        return mapToDto(product);
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

        dto.setFileUrls(fileRepo.findByProductId(product.getProductId()).stream()
                .map(ProductFile::getFileUrl)
                .collect(Collectors.toList()));

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

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                JsonNode node = new ObjectMapper().readTree(json);
                String ipfsHash = node.get("IpfsHash").asText();
                return "https://gateway.pinata.cloud/ipfs/" + ipfsHash;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Pinata", e);
        }
    }
}
