// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/model/dto/AuthResponse.java
package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    // ✅ FIX: Changed from Integer to Long to match the Auth entity.
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImage;
}