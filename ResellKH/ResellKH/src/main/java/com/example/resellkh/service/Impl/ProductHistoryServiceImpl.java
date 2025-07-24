package com.example.resellkh.service.Impl;

import com.example.resellkh.model.entity.ProductHistory;
import com.example.resellkh.repository.ProductHistoryRepo;
import com.example.resellkh.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductHistoryServiceImpl implements ProductHistoryService {

    private final ProductHistoryRepo historyRepo;

    @Override
    public void recordHistory(Long productId, String message) {
        ProductHistory history = new ProductHistory();
        history.setProductId(productId);
        history.setMessage(message);
        history.setUpdatedAt(LocalDateTime.now());
        historyRepo.addHistory(history);
    }

    @Override
    public List<ProductHistory> getHistory(Long productId) {
        return historyRepo.getHistoryByProductId(productId);
    }

    @Override
    public List<ProductHistory> getAllProductHistory() {
        return historyRepo.getAllProductHistory();
    }
}