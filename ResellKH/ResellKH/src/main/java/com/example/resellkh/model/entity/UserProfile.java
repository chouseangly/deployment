package com.example.resellkh.model.entity;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfile {
    private Long profileId;
    private Long userId;
    private String gender;
    private String phoneNumber;
    private String profileImage;
    private String coverImage;
    private LocalDate birthday;
    private String Address;
}

