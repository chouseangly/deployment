package com.example.resellkh.model.entity;
import lombok.*;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor

@Data
@Builder
public class UserProfile {
    private Long profileId;
    private Long userId;
    private String gender;
    private String phoneNumber;
    private String profileImage;
    private String coverImage;
    private LocalDate birthday;
    private String Address;
    private String telegramUrl;
    private String slogan;
    private String userName;
    private String firstName;
    private String lastName;
}

