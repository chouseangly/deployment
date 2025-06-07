package com.example.resellkh.service;
import com.example.resellkh.model.entity.ProductHistory;
import java.util.List;

public interface ProductHistoryService {
    void recordHistory(Integer productId, String message);
    List<ProductHistory> getHistory(Integer productId);

    List<ProductHistory> getAllProductHistory();
}
