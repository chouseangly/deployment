package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private Integer userId;
    private String email;
    private String firstName;
    private String lastName;
}
