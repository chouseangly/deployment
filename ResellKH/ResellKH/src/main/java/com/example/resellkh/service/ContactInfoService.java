package com.example.resellkh.service;

import com.example.resellkh.model.dto.ContactInfoRequest;
import com.example.resellkh.model.entity.ContactInfo;

import java.util.List;

public interface ContactInfoService {
    ContactInfo addContactInfo(ContactInfoRequest contactInfoRequest);

    List<ContactInfo> getAllContactInfo();

    List<ContactInfo> getContactByUserId(Long userId);
}
