package com.zyvora.zyvora_backend.service;

public interface EmailService {
    void sendOtpEmail(String to, String code, long expirationMinutes);
}