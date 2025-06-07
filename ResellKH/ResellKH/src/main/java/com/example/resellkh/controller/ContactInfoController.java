package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.dto.ContactInfoRequest;
import com.example.resellkh.model.entity.ContactInfo;
import com.example.resellkh.service.ContactInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contactInfo")
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfoService contactInfoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactInfo>> addContactInfo(@RequestBody ContactInfoRequest contactInfoRequest) {
        ContactInfo contactInfo = contactInfoService.addContactInfo(contactInfoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Contact Info added successfully",
                        contactInfo,
                        HttpStatus.CREATED.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactInfo>>> getAllContactInfo() {
        List<ContactInfo> allContacts = contactInfoService.getAllContactInfo();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "All user contact info retrieved successfully",
                        allContacts,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<ContactInfo>>> getContactInfoByUserId(@PathVariable Long userId) {
        List<ContactInfo> contacts = contactInfoService.getContactByUserId(userId);
        String message = contacts == null || contacts.isEmpty()
                ? "No contact info found for user ID: " + userId
                : "Contact info retrieved successfully";

        return ResponseEntity.ok(
                new ApiResponse<>(
                        message,
                        contacts,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }
}
