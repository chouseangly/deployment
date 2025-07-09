package com.example.resellkh.controller;

import com.example.resellkh.model.dto.ApiResponse;
import com.example.resellkh.model.entity.UserProfile;
import com.example.resellkh.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> createUserProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate birthday,
            @RequestParam("address") String address,
            @RequestPart("profileImage") MultipartFile profileImage,
            @RequestPart("coverImage") MultipartFile coverImage
    ) {
        try {
            UserProfile created = profileService.createUserProfile(userId, gender, phoneNumber, birthday, address, profileImage, coverImage);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("User profile created/updated successfully", created, HttpStatus.CREATED.value(), LocalDateTime.now()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to process image", null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
        }
    }

    @PutMapping(path = "edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @RequestParam("userId") Long userId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String telegramUrl,
            @RequestParam(required = false) String slogan,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage
    ) {
        try {
            UserProfile updated = profileService.updateUserProfileWithImage(
                    userId, gender, phoneNumber, birthday, address, telegramUrl,
                    slogan, userName, firstName, lastName, profileImage, coverImage);
            return ResponseEntity.ok(new ApiResponse<>("User profile updated successfully", updated, HttpStatus.OK.value(), LocalDateTime.now()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to update image", null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
        }
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfile>>> getUserProfiles() {
        List<UserProfile> userProfiles = profileService.getUserProfiles();

        if (userProfiles == null || userProfiles.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponse<>("No user profiles found", Collections.emptyList(), HttpStatus.OK.value(), LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>("Get user profiles successfully", userProfiles, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfile>> getProfile(@PathVariable Long userId) {
        UserProfile userProfile = profileService.getProfile(userId);

        if (userProfile == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>("User profile not found", null, HttpStatus.OK.value(), LocalDateTime.now())
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>("User profile fetched successfully", userProfile, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@PathVariable Long userId) {
        String result = profileService.deleteProfile(userId);
        return ResponseEntity.ok(
                new ApiResponse<>("User profile deleted", result, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}
