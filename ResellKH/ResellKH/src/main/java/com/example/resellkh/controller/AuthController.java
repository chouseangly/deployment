package com.example.resellkh.controller;

import com.example.resellkh.model.dto.*;
import com.example.resellkh.model.entity.Auth;
import com.example.resellkh.jwt.JwtService;
import com.example.resellkh.service.OtpService;
import com.example.resellkh.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

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
            return ResponseEntity.ok("OTP verified. User activated.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
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

        String token = jwtService.generateToken(auth);
        return ResponseEntity.ok(new JwtResponse(token));
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
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "User not found",
                            null,
                            HttpStatus.OK.value(),
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
    public ResponseEntity<ApiResponse<Auth>> registerWithGoogle(@RequestBody GoogleUserDto googleUserDto) {
        Auth auth = authService.registerWithGoogle(googleUserDto);
        return ResponseEntity.ok(new ApiResponse<>(
                "Google registration successful",
                auth,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        ));
    }

}

