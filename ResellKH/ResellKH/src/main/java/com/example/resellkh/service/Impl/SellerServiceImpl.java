// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/service/Impl/SellerServiceImpl.java
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
        Seller seller = Seller.builder()
                .userId(request.getUserId())
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .businessAddress(request.getBusinessAddress())
                .businessDescription(request.getBusinessDescription())
                .expectedRevenue(request.getExpectedRevenue())
                .bankName(request.getBankName())
                .bankAccountName(request.getBankAccountName())
                .bankAccountNumber(request.getBankAccountNumber())
                .build();
        sellerRepo.insertSeller(seller);
        userProfileRepo.updateIsSeller(request.getUserId(), true);
        return sellerRepo.findByUserId(request.getUserId());
    }

    @Override
    @Transactional
    public Seller getSellerByUserId(Long userId) {
        return sellerRepo.findByUserId(userId);
    }


    @Transactional
    public Seller getSellerBySellerId(Long sellerId) {
        return sellerRepo.getSellerBySellerId(sellerId);
    }

    // --- REFACTORED AND CORRECTED UPDATE METHOD ---
    @Override
    @Transactional
    public Seller updateSeller(Long sellerId, SellerRequest request) {
        // To be absolutely certain, we will construct a new Seller object
        // with the exact data to be updated.
        Seller sellerToUpdate = new Seller();

        // IMPORTANT: Set the ID for the WHERE clause of the UPDATE statement
        sellerToUpdate.setSellerId(sellerId);

        // Set all other fields from the incoming request
        sellerToUpdate.setBusinessName(request.getBusinessName());
        sellerToUpdate.setBusinessType(request.getBusinessType());
        sellerToUpdate.setBusinessAddress(request.getBusinessAddress());
        sellerToUpdate.setBusinessDescription(request.getBusinessDescription());
        sellerToUpdate.setExpectedRevenue(request.getExpectedRevenue());
        sellerToUpdate.setBankName(request.getBankName());
        sellerToUpdate.setBankAccountName(request.getBankAccountName());
        sellerToUpdate.setBankAccountNumber(request.getBankAccountNumber());

        // Now, pass this clean object to the repository
        int rowsAffected = sellerRepo.updateSeller(sellerToUpdate);

        // This check remains crucial. If it fails, something is wrong with the sellerId.
        if (rowsAffected == 0) {
            throw new RuntimeException("Update failed: No seller found with ID " + sellerId + " in the database.");
        }

        // Return the updated object
        return sellerToUpdate;
    }
}