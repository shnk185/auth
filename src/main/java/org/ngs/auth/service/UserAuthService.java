package org.ngs.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.Token;
import org.ngs.auth.dto.request.UserCreateRequest;
import org.ngs.auth.dto.request.UserLoginRequest;
import org.ngs.auth.dto.request.UserRefreshSessionRequest;
import org.ngs.auth.dto.request.UserVerifyRequest;
import org.ngs.auth.dto.response.*;
import org.ngs.auth.entity.UserEmailAuthEntity;
import org.ngs.auth.entity.UserEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.repository.UserEmailAuthRepository;
import org.ngs.auth.repository.UserRepository;
import org.ngs.auth.util.KeyUtil;
import org.ngs.auth.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailAuthRepository userEmailAuthRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CookieService cookieService;

    public void logoutUser(HttpServletResponse httpServletResponse) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        log.info("logging out user {}", userId);
        redisTemplate.opsForValue().set(RedisKeyUtil.generateLogoutKey(userId), String.valueOf(new Date().getTime()), 1, TimeUnit.HOURS);
        tokenService.revokeRefreshToken(userId);
        httpServletResponse.sendRedirect("/");
    }

    public void refreshSession(UserRefreshSessionRequest userRefreshSessionRequest, HttpServletResponse httpServletResponse) {
        String refreshToken = userRefreshSessionRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("invalid refresh token");
        }
        Long userId = tokenService.validateRefreshToken(userRefreshSessionRequest.getRefreshToken());
        redisTemplate.delete(RedisKeyUtil.generateLogoutKey(userId));
        UserEmailAuthEntity userEmailAuthEntity = userEmailAuthRepository.findByUserId(userId);
        cookieService.setAccessAndRefreshTokenCookiesInResponse(userId,
                userEmailAuthEntity == null ? null : userEmailAuthEntity.getEmail(), httpServletResponse);
    }

    public void deleteUser(HttpServletResponse httpServletResponse) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        logoutUser(httpServletResponse);
        UserEntity userEntity = userRepository.findByIdAndDeletedFalse(userId).orElseThrow();
        userEntity.setDeleted(true);
        userRepository.save(userEntity);
    }
}
