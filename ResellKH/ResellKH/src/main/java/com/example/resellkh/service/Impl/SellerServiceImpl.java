package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;
import com.example.resellkh.repository.SellerRepo;
import com.example.resellkh.repository.UserProfileRepo;
import com.example.resellkh.service.SellerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepo sellerRepo;
    private final UserProfileRepo userProfileRepo;

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
        userProfileRepo.updateIsSeller(request.getUserId(), true);

        // Return the complete entity with generated ID
        return sellerRepo.findById(seller.getSellerId());
    }



    @Override
    @Transactional
    public Seller updateSeller(Long sellerId, SellerRequest request) {
        // FIX: Changed findById to the correct method name: getSellerBySellerId
        Seller existingSeller = sellerRepo.getSellerBySellerId(sellerId);

        if (existingSeller == null) {
            // This makes the error message clearer if the seller isn't found
            throw new RuntimeException("Seller not found with ID: " + sellerId);
        }

        // Update fields from the request
        existingSeller.setBusinessName(request.getBusinessName());
        existingSeller.setBusinessType(request.getBusinessType());
        existingSeller.setBusinessAddress(request.getBusinessAddress());
        existingSeller.setBusinessDescription(request.getBusinessDescription());
        existingSeller.setExpectedRevenue(request.getExpectedRevenue());
        existingSeller.setBankName(request.getBankName());
        existingSeller.setBankAccountName(request.getBankAccountName());
        existingSeller.setBankAccountNumber(request.getBankAccountNumber());

        sellerRepo.updateSeller(existingSeller);

        return existingSeller;
    }
    @Transactional
    @Override
    public Seller getSellerByUserId(Long userId) {
        return sellerRepo.findByUserId(userId);
    }
}