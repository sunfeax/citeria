package com.sunfeax.citeria.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.entity.RefreshTokenEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.exception.UserNotFoundException;
import com.sunfeax.citeria.repository.RefreshTokenRepository;
import com.sunfeax.citeria.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTES = 64;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.jwt.refreshExpiration}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public String createRefreshToken(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        Instant expiryDate = buildExpiryDate();

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setTokenHash(tokenHash);
                    existingToken.setExpiryDate(expiryDate);
                    return existingToken;
                })
                .orElseGet(() -> RefreshTokenEntity.builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiryDate(expiryDate)
                        .build());

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshTokenEntity> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return refreshTokenRepository.findByTokenHash(hashToken(token));
    }

    @Transactional
    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public String rotateRefreshToken(RefreshTokenEntity token) {
        String currentTokenHash = token.getTokenHash();
        Instant newExpiryDate = buildExpiryDate();
        String rawToken = generateRawToken();
        String newTokenHash = hashToken(rawToken);

        int updated = refreshTokenRepository.rotateTokenIfValid(
            currentTokenHash,
            newTokenHash,
            newExpiryDate,
            Instant.now()
        );

        if (updated != 1) {
            throw new UnauthorizedException("Refresh token is invalid or already used");
        }

        token.setTokenHash(newTokenHash);
        token.setExpiryDate(newExpiryDate);
        return rawToken;
    }

    @Transactional
    public void deleteByToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        refreshTokenRepository.deleteByTokenHash(hashToken(token));
    }

    private Instant buildExpiryDate() {
        return Instant.now().plusMillis(refreshTokenDuration);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Scheduled(fixedDelayString = "${app.jwt.refreshCleanupIntervalMs:3600000}")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        if (deleted > 0) {
            log.debug("Deleted {} expired refresh tokens", deleted);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
