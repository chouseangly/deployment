// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/service/SellerService.java
package com.example.resellkh.service;

import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;

public interface SellerService {
    Seller createSeller(SellerRequest request);
    Seller getSellerByUserId(Long userId);
    Seller updateSeller(Long sellerId, SellerRequest request);
    // Keep this for consistency if needed elsewhere
}