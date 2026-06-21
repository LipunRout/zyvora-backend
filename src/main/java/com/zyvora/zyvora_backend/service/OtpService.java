package com.zyvora.zyvora_backend.service;

import com.zyvora.zyvora_backend.dto.response.AuthResponse;

public interface OtpService {

    void sendOtp(String email);

    AuthResponse verifyOtp(String email, String code);
}