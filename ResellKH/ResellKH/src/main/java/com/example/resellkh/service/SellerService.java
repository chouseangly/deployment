package com.example.resellkh.service;

import com.example.resellkh.model.dto.SellerRequest;
import com.example.resellkh.model.entity.Seller;

public interface SellerService {


    Seller createSeller(SellerRequest request);


    Seller updateSeller(Long sellerId, SellerRequest request);

    Seller getSellerByUserId(Long userId);
}
