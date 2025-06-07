package com.example.resellkh.model.entity;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    private Integer id;
    private String email;
    private String otp;
    private LocalDateTime createdAt;
    private boolean verified;
}