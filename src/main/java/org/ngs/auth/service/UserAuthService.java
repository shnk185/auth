package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.Token;
import org.ngs.auth.dto.request.UserRefreshSessionRequest;
import org.ngs.auth.dto.response.UserDeleteAccountResponse;
import org.ngs.auth.dto.response.UserInfoResponse;
import org.ngs.auth.dto.response.UserLogoutResponse;
import org.ngs.auth.dto.response.UserRefreshSessionResponse;
import org.ngs.auth.entity.UserEmailAuthEntity;
import org.ngs.auth.entity.UserEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.repository.UserEmailAuthRepository;
import org.ngs.auth.repository.UserRepository;
import org.ngs.auth.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private JwtService jwtService;

    public UserLogoutResponse logoutUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        log.info("logging out user {}", userId);
        redisTemplate.opsForValue().set(RedisKeyUtil.generateLogoutKey(userId), String.valueOf(new Date().getTime()), 1, TimeUnit.HOURS);
        long revokedAt = tokenService.revokeRefreshToken(userId);
        return new UserLogoutResponse(userId, true, revokedAt);
    }

    public UserRefreshSessionResponse refreshSession(UserRefreshSessionRequest userRefreshSessionRequest) {
        String refreshToken = userRefreshSessionRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("invalid refresh token");
        }
        Long userId = tokenService.validateRefreshToken(userRefreshSessionRequest.getRefreshToken());
        redisTemplate.delete(RedisKeyUtil.generateLogoutKey(userId));
        UserEmailAuthEntity userEmailAuthEntity = userEmailAuthRepository.findByUserId(userId);
        Token jwtToken = jwtService.generateToken(userId, userEmailAuthEntity.getEmail());
        Token newRefreshToken = tokenService.generateRefreshToken(userId);
        return new UserRefreshSessionResponse(jwtToken, newRefreshToken);
    }

    public UserDeleteAccountResponse deleteUser()  {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        logoutUser();
        UserEntity userEntity = userRepository.findByIdAndDeletedFalse(userId).orElseThrow();
        userEntity.setDeleted(true);
        userRepository.save(userEntity);
        return new UserDeleteAccountResponse(userId, true);
    }

    public UserInfoResponse fetchUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        UserEntity userEntity = userRepository.findByIdAndDeletedFalse(userId).orElseThrow();
        UserInfoResponse userInfoResponse = UserInfoResponse.builder().userId(userEntity.getId())
                .authMethod(userEntity.getAuthMethod()).userName(userEntity.getUserName())
                .build();
        if (AuthMethod.EMAIL.equals(userEntity.getAuthMethod())) {
            UserEmailAuthEntity userEmailAuthEntity = userEmailAuthRepository.findByUserId(userId);
            userInfoResponse.setEmail(userEmailAuthEntity.getEmail());
        }
        return userInfoResponse;
    }
}
