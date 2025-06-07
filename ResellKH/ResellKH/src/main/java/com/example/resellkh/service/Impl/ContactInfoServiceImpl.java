package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.ContactInfoRequest;
import com.example.resellkh.model.entity.ContactInfo;
import com.example.resellkh.repository.ContactInfoRepo;
import com.example.resellkh.service.ContactInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactInfoServiceImpl implements ContactInfoService {

    private final ContactInfoRepo contactInfoRepo;

    @Override
    public ContactInfo addContactInfo(ContactInfoRequest contactInfoRequest) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setUserId(contactInfoRequest.getUserId());
        contactInfo.setTelegramUrl(contactInfoRequest.getTelegramUrl());
        contactInfoRepo.addContactInfo(contactInfo);
        return contactInfo;
    }

    @Override
    public List<ContactInfo> getAllContactInfo() {
        return contactInfoRepo.getAllContactInfo();
    }

    @Override
    public List<ContactInfo> getContactByUserId(Long userId) {
        return contactInfoRepo.getContactByUserId(userId);
    }
}
