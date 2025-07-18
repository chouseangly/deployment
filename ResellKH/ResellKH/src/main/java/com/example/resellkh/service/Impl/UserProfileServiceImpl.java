package com.example.resellkh.service.Impl;

import com.example.resellkh.model.dto.UserProfileRequest;
import com.example.resellkh.model.entity.UserProfile;
import com.example.resellkh.repository.UserProfileRepo;
import com.example.resellkh.service.UserProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepo userProfileRepo;

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    @Override
    public UserProfile createUserProfile(Long userId, String gender, String phoneNumber,
                                         LocalDate birthday, String address,
                                         MultipartFile profileImage, MultipartFile coverImage) throws IOException {

        String profileImagePath = uploadFileToPinata(profileImage);
        String coverImagePath = uploadFileToPinata(coverImage);

        UserProfileRequest request = new UserProfileRequest();
        request.setUserId(userId);
        request.setGender(gender);
        request.setPhoneNumber(phoneNumber);
        request.setBirthday(birthday);
        request.setAddress(address);
        request.setProfileImage(profileImagePath);
        request.setCoverImage(coverImagePath);

        UserProfile existing = userProfileRepo.getProfileByUserId(userId);
        if (existing == null) {
            userProfileRepo.createUserProfile(request);
        } else {
            userProfileRepo.updateUserProfile(request);
        }

        return userProfileRepo.getProfileByUserId(userId);
    }
    @Override
    public UserProfile updateUserProfileWithImage(Long userId, String gender, String phoneNumber,
                                                  LocalDate birthday, String address, String telegramUrl,
                                                  String slogan, String userName, String firstName, String lastName,
                                                  MultipartFile profileImage, MultipartFile coverImage) throws IOException {

        UserProfile existing = userProfileRepo.getProfileByUserId(userId);
        if (existing == null) return null;

        // Update fields if provided
        if (gender != null && !gender.isEmpty()) existing.setGender(gender);
        if (phoneNumber != null && !phoneNumber.isEmpty()) existing.setPhoneNumber(phoneNumber);
        if (birthday != null) existing.setBirthday(birthday);
        if (address != null && !address.isEmpty()) existing.setAddress(address);
        if (telegramUrl != null && !telegramUrl.isEmpty()) existing.setTelegramUrl(telegramUrl);
        if (slogan != null && !slogan.isEmpty()) existing.setSlogan(slogan);
        if (userName != null && !userName.isEmpty()) existing.setUserName(userName);
        if (firstName != null && !firstName.isEmpty()) existing.setFirstName(firstName);
        if (lastName != null && !lastName.isEmpty()) existing.setLastName(lastName);

        // Handle profile and cover image uploads
        if (profileImage != null && !profileImage.isEmpty()) {
            existing.setProfileImage(uploadFileToPinata(profileImage));
        }
        if (coverImage != null && !coverImage.isEmpty()) {
            existing.setCoverImage(uploadFileToPinata(coverImage));
        }

        // Convert updated entity to request DTO
        UserProfileRequest request = new UserProfileRequest();
        request.setUserId(existing.getUserId());
        request.setGender(existing.getGender());
        request.setPhoneNumber(existing.getPhoneNumber());
        request.setBirthday(existing.getBirthday());
        request.setAddress(existing.getAddress());
        request.setTelegramUrl(existing.getTelegramUrl());
        request.setSlogan(existing.getSlogan());
        request.setUserName(existing.getUserName());
        request.setFirstName(existing.getFirstName());
        request.setLastName(existing.getLastName());
        request.setProfileImage(existing.getProfileImage());
        request.setCoverImage(existing.getCoverImage());


        userProfileRepo.updateUserProfile(request);


        return userProfileRepo.getProfileByUserId(userId);
    }


    private static UserProfile getUserProfile(UserProfile existing) {
        return existing;
    }


    @Override
    public List<UserProfile> getUserProfiles() {
        return userProfileRepo.getUserProfiles();
    }



    @Override
    public UserProfile getProfile(Long userId) {
        return userProfileRepo.getProfileByUserId(userId);
    }

    @Override
    public String deleteProfile(Long userId) {
        return userProfileRepo.deleteProfile(userId) > 0 ? "Deleted" : "Not Found";
    }

    private String uploadFileToPinata(MultipartFile file) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(PINATA_URL);
            post.setHeader("pinata_api_key", pinataApiKey);
            post.setHeader("pinata_secret_api_key", pinataSecretApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), ContentType.DEFAULT_BINARY,
                    UUID.randomUUID() + "_" + file.getOriginalFilename());

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                JsonNode node = new ObjectMapper().readTree(json);
                String ipfsHash = node.get("IpfsHash").asText();
                return "https://gateway.pinata.cloud/ipfs/" + ipfsHash;
            }
        }
    }
    @Override
    public void createUserProfileAfterVerify(UserProfile userProfile){
        userProfileRepo.createUserProfileAfterVerify(userProfile);
    }
    @Override
    public boolean existsByUserId(Long userId) {
        return userProfileRepo.existsByUserId(userId);
    }



}
