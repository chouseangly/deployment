package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;
import com.example.resellkh.repository.SellerRepo;
import com.example.resellkh.service.SellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepo sellerRepo;

    @Override
    @Transactional
    public Seller createSeller(SellerRequest request) {
        // Convert DTO to Entity
        Seller seller = Seller.builder()
                .userId(request.getUserId())
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .businessAddress(request.getBusinessAddress())
                .businessDescription(request.getBusinessDescription())
                .expectedRevenue(Double.valueOf(String.valueOf(request.getExpectedRevenue())))
                .bankName(request.getBankName())
                .bankAccountName(request.getBankAccountName())
                .bankAccountNumber(request.getBankAccountNumber())
                .build();

        // Insert and get generated ID
        sellerRepo.insertSeller(seller);

        // Return the complete entity with generated ID
        return sellerRepo.findById(seller.getSellerId());
    }
    @Transactional
    @Override
    public Seller getSellerBySellerId(Long sellerId) {
        return sellerRepo.getSellerBySellerId(sellerId);
    }
}