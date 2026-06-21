package com.zyvora.zyvora_backend.repository;



import com.zyvora.zyvora_backend.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // Latest non-used token for an email - used during verify
    Optional<OtpToken> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}