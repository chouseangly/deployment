package com.example.resellkh.service;

import com.example.resellkh.model.dto.OtpRequest;

public interface OtpService {
    void sendOtp(String email);
    boolean verifyOtp(OtpRequest otpRequest);
}