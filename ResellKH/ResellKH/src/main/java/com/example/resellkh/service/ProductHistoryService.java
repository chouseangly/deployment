package com.example.resellkh.service;
import com.example.resellkh.model.entity.ProductHistory;
import java.util.List;

public interface ProductHistoryService {
    void recordHistory(Long productId, String message);
    List<ProductHistory> getHistory(Long productId);
    List<ProductHistory> getAllProductHistory();
}