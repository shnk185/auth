package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.Token;
import org.ngs.auth.entity.UserTokenMappingEntity;
import org.ngs.auth.enums.TokenType;
import org.ngs.auth.repository.UserTokenMappingRepository;
import org.ngs.auth.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        revokeRefreshToken(userId);
        String generatedToken = generateToken();
        log.info("generated refresh token {} userId {}", generatedToken, userId);
        String tokenHash = TokenUtil.hashToken(generatedToken);
        log.info("generated refresh token hash {} userId {}", tokenHash, userId);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationTimeMs);
        UserTokenMappingEntity userTokenMappingEntity = new UserTokenMappingEntity(userId, tokenHash, TokenType.REFRESH,
                expiry, null);
        userTokenMappingRepository.save(userTokenMappingEntity);
        return new Token(TokenType.REFRESH, generatedToken, expiry);
    }

    public String generateToken() {
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public long revokeRefreshToken(Long userId) {
        UserTokenMappingEntity userTokenMappingEntity = userTokenMappingRepository.findByUserIdAndRevokedAtNull(userId);
        if (userTokenMappingEntity == null) return -1L;
        userTokenMappingEntity.setRevokedAt(new Date().getTime());
        userTokenMappingRepository.save(userTokenMappingEntity);
        log.info("refresh token for user {} revoked", userId);
        return userTokenMappingEntity.getRevokedAt();
    }

    public Long validateRefreshToken(String refreshToken) {
        String hashedToken = TokenUtil.hashToken(refreshToken);
        UserTokenMappingEntity userTokenMappingEntity = userTokenMappingRepository.findByTokenHashAndRevokedAtNull(hashedToken);
        if (userTokenMappingEntity == null) {
            throw new RuntimeException("invalid refresh token");
        } else if (userTokenMappingEntity.getExpiresAt().before(new Date())) {
            throw new RuntimeException("expired refresh token");
        }
        return userTokenMappingEntity.getUserId();
    }
}
