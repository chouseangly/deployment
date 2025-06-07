package com.example.resellkh.service;

import com.example.resellkh.model.dto.UserProfileRequest;
import com.example.resellkh.model.entity.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

public interface UserProfileService {
    UserProfile getProfile(Long userId);
    String deleteProfile(Long userId);
    UserProfile createUserProfile(Long userId, String gender, String phoneNumber, LocalDate birthday, String address, MultipartFile profileImage, MultipartFile coverImage) throws IOException;
    UserProfile updateUserProfileWithImage(Long userId, String gender, String phoneNumber, LocalDate birthday, String address, MultipartFile profileImage, MultipartFile coverImage) throws IOException;

    UserProfile getUserProfiles();
}
