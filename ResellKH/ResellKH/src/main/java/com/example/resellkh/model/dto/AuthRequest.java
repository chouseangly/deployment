package com.example.resellkh.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}