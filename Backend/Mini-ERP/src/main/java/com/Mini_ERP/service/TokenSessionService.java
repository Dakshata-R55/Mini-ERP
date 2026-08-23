package com.Mini_ERP.service;

import com.Mini_ERP.model.ActiveToken;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.repository.ActiveTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private final ActiveTokenRepository activeTokenRepository;

    @Transactional
    public void register(AppUser user, String jti, Instant expiresAt) {
        activeTokenRepository.save(ActiveToken.builder()
                .jti(jti)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build());
    }

    public boolean isActive(String jti) {
        return activeTokenRepository.findByJtiAndRevokedFalse(jti)
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    @Transactional
    public void revoke(String jti) {
        activeTokenRepository.findById(jti).ifPresent(token -> {
            token.setRevoked(true);
            activeTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        activeTokenRepository.revokeAllByUserId(userId);
    }
}