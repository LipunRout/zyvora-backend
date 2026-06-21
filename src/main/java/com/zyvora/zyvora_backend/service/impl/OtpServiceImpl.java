package com.zyvora.zyvora_backend.service.impl;

import com.zyvora.zyvora_backend.dto.response.AuthResponse;
import com.zyvora.zyvora_backend.entity.OtpToken;
import com.zyvora.zyvora_backend.entity.User;
import com.zyvora.zyvora_backend.exception.InvalidOtpException;
import com.zyvora.zyvora_backend.repository.OtpTokenRepository;
import com.zyvora.zyvora_backend.repository.UserRepository;
import com.zyvora.zyvora_backend.security.jwt.JwtTokenProvider;
import com.zyvora.zyvora_backend.service.EmailService;
import com.zyvora.zyvora_backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private long otpExpirationMinutes;

    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void sendOtp(String email) {
        String code = generateCode();

        OtpToken token = OtpToken.builder()
                .email(email)
                .codeHash(encoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .attemptCount(0)
                .build();

        otpTokenRepository.save(token);

        emailService.sendOtpEmail(email, code, otpExpirationMinutes);
    }

    @Override
    public AuthResponse verifyOtp(String email, String code) {
        OtpToken token = otpTokenRepository
                .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidOtpException("No active OTP found for this email. Please request a new code."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("This OTP has expired. Please request a new code.");
        }

        if (token.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new InvalidOtpException("Too many incorrect attempts. Please request a new code.");
        }

        if (!encoder.matches(code, token.getCodeHash())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            otpTokenRepository.save(token);
            throw new InvalidOtpException("Incorrect OTP code.");
        }

        // Mark used so it can't be replayed
        token.setUsed(true);
        otpTokenRepository.save(token);

        // Find or create the user - this is the DB write step
        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setLastActive(LocalDateTime.now());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(email.split("@")[0]) // placeholder until profile setup
                            .provider(User.AuthProvider.LOCAL)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        String jwt = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName(), user.getPicture());

        return AuthResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    private String generateCode() {
        int code = 1000 + random.nextInt(9000); // always 4 digits: 1000-9999
        return String.valueOf(code);
    }
}