package org.ngs.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.Token;
import org.ngs.auth.enums.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;

    @Value("${app.jwt.expiration-ms}")
    private Long expirationMS;

    public JwtService(@Value("${app.jwt.secret}") String jwtSecret) {
        byte[] decoded = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(decoded);
    }

    public Token generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMS);
        String jwt = Jwts.builder().subject(String.valueOf(userId)).claim("email", email).issuedAt(now)
                .expiration(expiry).signWith(secretKey).compact();
        log.info("issued jwt {} for userId {}", jwt, userId);
        return new Token(TokenType.ACCESS, jwt, expiry, TimeUnit.MILLISECONDS.toSeconds(expirationMS));
    }
}
