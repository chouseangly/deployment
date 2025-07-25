package com.example.resellkh.controller;

import com.example.resellkh.model.dto.*;
import com.example.resellkh.model.entity.Auth;
import com.example.resellkh.jwt.JwtService;
import com.example.resellkh.model.entity.Notification;
import com.example.resellkh.model.entity.UserProfile;
import com.example.resellkh.service.Impl.AuthServiceImpl;
import com.example.resellkh.service.Impl.UserProfileServiceImpl;
import com.example.resellkh.service.NotificationService;
import com.example.resellkh.service.OtpService;
import com.example.resellkh.service.AuthService;
import com.example.resellkh.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthServiceImpl authServiceImpl;
    private final UserProfileService userProfileService;
    private final NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) {
        Auth auth = new Auth();
        auth.setFirstName(authRequest.getFirstName());
        auth.setLastName(authRequest.getLastName());
        auth.setEmail(authRequest.getEmail());
        auth.setUserName(authRequest.getFirstName() + " " + authRequest.getLastName());
        auth.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        auth.setRole("USER");
        auth.setCreatedAt(LocalDateTime.now());
        auth.setEnabled(false);

        authService.registerUser(auth);
        otpService.sendOtp(auth.getEmail());

        return ResponseEntity.ok("User registered successfully. OTP sent.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpService.verifyOtp(otpRequest);
        if (isValid) {
            authService.enableUser(otpRequest.getEmail());

            Auth auth = authService.findByEmail(otpRequest.getEmail());

            Long userId = auth.getUserId().longValue();

            if (!userProfileService.existsByUserId(userId)) {
                UserProfile profile = UserProfile.builder()
                        .userId(userId)
                        .firstName(auth.getFirstName())
                        .lastName(auth.getLastName())
                        .userName(auth.getUserName())
                        .build();

                userProfileService.createUserProfileAfterVerify(profile);
            }

            return ResponseEntity.ok("OTP verified. User activated.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
    }


    @GetMapping("/finduserbyemail")
    public ResponseEntity<Auth> findUserByEmail(@RequestParam String email) {
        Auth auth = authService.findByEmail(email);
        return ResponseEntity.ok(auth);
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody OtpRequest otpRequest) {
        otpService.sendOtp(otpRequest.getEmail());
        return ResponseEntity.ok("OTP resent successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest login) {
        Auth auth = authService.findByEmail(login.getEmail());

        if (auth == null || !passwordEncoder.matches(login.getPassword(), auth.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        if (!auth.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Account not verified"));
        }
        Notification notification = Notification.builder()
                .userId(Long.valueOf(auth.getUserId()))
                .title("Welcome to ResellKH")
                .content("Welcome to ResellKH! We’re excited to have you join our community. As a new member, you can explore great deals, post your products, and connect with trusted buyers and sellers. Stay updated with the latest promotions, features, and security tips. Thank you for choosing ResellKH — let’s grow together!")
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                .build();
        notificationService.createNotificationWithType(notification);
        System.out.println("Login endpoint called for user!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + login.getEmail());


        String token = jwtService.generateToken(auth);
        String role = auth.getRole();

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", role,
                "userId", auth.getUserId(),
                "firstName", auth.getFirstName(),
                "lastName", auth.getLastName(),
                "userName", auth.getUserName(),
                "email", auth.getEmail(),
                "message", "Login successful"
        ));
    }




    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpService.verifyOtp(otpRequest);

        if (isValid) {
            return ResponseEntity.ok("OTP verified");
        } else {
            return ResponseEntity.ok("Invalid or expired OTP");
        }
    }

    @PutMapping("/reset-new-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        Auth user = authService.findByEmail(resetRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(
                            "User not found",
                            null,
                            HttpStatus.NOT_FOUND.value(),
                            LocalDateTime.now()
                    )
            );
        }

        String encodedPassword = passwordEncoder.encode(resetRequest.getNewPassword());

        Auth updated = authService.resetPassword(resetRequest.getEmail(), encodedPassword);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Password reset successfully",
                        updated,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleUserDto googleUserDto) {
        AuthResponse authResponse = authService.registerWithGoogle(googleUserDto);
        Map<String, Object> response = new HashMap<>();
        response.put("payload", authResponse); // <-- Wrap in 'payload'
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<Auth>>> getAllUser() {
        List<Auth> users = authServiceImpl.getAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                "Get all users successfully",
                users,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        ));
    }


}

