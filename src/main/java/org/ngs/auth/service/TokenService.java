package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.Token;
import org.ngs.auth.entity.UserTokenMappingEntity;
import org.ngs.auth.enums.TokenType;
import org.ngs.auth.repository.UserTokenMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class TokenService {

    @Autowired
    private UserTokenMappingRepository userTokenMappingRepository;

    @Autowired
    private SecureRandom secureRandom;

    @Value("${token.refresh.expiration-time-ms}")
    private Long refreshTokenExpirationTimeMs;

    public Token generateRefreshToken(Long userId) {
        String generatedToken = generateToken();
        log.info("generated refresh token {} userId {}", generatedToken, userId);
        String tokenHash = hashToken(generatedToken);
        log.info("generated refresh token hash {} userId {}", tokenHash, userId);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationTimeMs);
        UserTokenMappingEntity userTokenMappingEntity = UserTokenMappingEntity.builder()
                .tokenType(TokenType.REFRESH)
                .tokenHash(tokenHash)
                .expiresAt(expiry)
                .userId(userId)
                .build();
        userTokenMappingRepository.save(userTokenMappingEntity);
        return Token.builder()
                .tokenType(TokenType.REFRESH)
                .token(generatedToken)
                .expiresAt(expiry)
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_16));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("hash algorithm not found");
            throw new RuntimeException("hash algorithm not found");
        }
    }

    public String generateToken() {
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public long revokeRefreshToken(Long userId) {
        UserTokenMappingEntity userTokenMappingEntity = userTokenMappingRepository.findByUserId(userId);
        userTokenMappingEntity.setRevokedAt(new Date().getTime());
        userTokenMappingRepository.save(userTokenMappingEntity);
        log.info("refresh token for user {} revoked", userId);
        return userTokenMappingEntity.getRevokedAt();
    }
}
