package org.ngs.auth.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.*;
import org.ngs.auth.entity.UserEmailAuthEntity;
import org.ngs.auth.entity.UserEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.enums.TokenType;
import org.ngs.auth.repository.UserEmailAuthRepository;
import org.ngs.auth.repository.UserRepository;
import org.ngs.auth.util.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailAuthRepository userEmailAuthRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenService tokenService;

    @Transactional
    public UserCreateResponse createUser(UserCreateRequest userCreateRequest) {
        UserEntity userEntity = new UserEntity(userCreateRequest.getUserName(),  AuthMethod.EMAIL, false);
        userRepository.save(userEntity);
        UserEmailAuthEntity userEmailAuthEntity = new UserEmailAuthEntity(userEntity.getId(), userCreateRequest.getEmail(),
                passwordEncoder.encode(userCreateRequest.getPassword()));
        userEmailAuthRepository.save(userEmailAuthEntity);

        redisTemplate.opsForValue().set(KeyUtil.generateSignUpVerifyKey(userEntity.getId()), "123456", 24, TimeUnit.HOURS);

        return new UserCreateResponse(userEntity.getId(), userEntity.getUserName(), userEntity.getAuthMethod(),
                userEntity.getVerified(), userEntity.isDeleted(), userEmailAuthEntity.getEmail());
    }

    public UserVerificationResponse verifyUser(UserVerifyRequest userVerifyRequest) {
        Long userId = userVerifyRequest.getUserId();
        String otp = redisTemplate.opsForValue().get(KeyUtil.generateSignUpVerifyKey(userId));
        if (otp == null) {
            throw new RuntimeException("verification failed");
        }

        boolean verified = otp.equals(userVerifyRequest.getVerificationCode());
        if (verified) {
            UserEntity userEntity = userRepository.findByIdAndDeletedFalse(userId).orElseThrow();
            userEntity.setVerified(true);
            userRepository.save(userEntity);
            return new UserVerificationResponse(userId, true, null);
        }
        Long remainingAttempts = getRemainingAttempts(userVerifyRequest);

        return new UserVerificationResponse(userId, false, remainingAttempts);
    }

    private Long getRemainingAttempts(UserVerifyRequest userVerifyRequest) {
        String verificationAttemptKey = KeyUtil.generateSignUpVerifyAttemptsKey(userVerifyRequest.getUserId());
        Long verificationAttempts = redisTemplate.opsForValue().decrement(verificationAttemptKey);
        if (verificationAttempts == -1) {
            redisTemplate.opsForValue().set(verificationAttemptKey, "4", 24, TimeUnit.HOURS);
            verificationAttempts = 4L;
        } else if (verificationAttempts == 0) {
                redisTemplate.delete(KeyUtil.generateSignUpVerifyKey(userVerifyRequest.getUserId()));
        }
        return verificationAttempts;
    }

    public UserLoginResponse loginUser(UserLoginRequest userLoginRequest) {
        if (userLoginRequest.getUserName() == null && userLoginRequest.getEmail() == null) {
            throw new RuntimeException("invalid credentials");
        }

        UserEntity userEntity = null;
        UserEmailAuthEntity userEmailAuthEntity = null;
        if (userLoginRequest.getUserName() != null) {
            userEntity = userRepository.findByUserNameAndDeletedFalse(userLoginRequest.getUserName());
            validateUser(userEntity);
            userEmailAuthEntity = userEmailAuthRepository.findByUserId(userEntity.getId());
        } else {
            userEmailAuthEntity = userEmailAuthRepository.findByEmail(userLoginRequest.getEmail());
            if (userEmailAuthEntity == null) {
                throw new RuntimeException("invalid credentials");
            }
            userEntity = userRepository.findByIdAndDeletedFalse(userEmailAuthEntity.getUserId()).orElseThrow();
            validateUser(userEntity);
        }

        if (!passwordEncoder.matches(userLoginRequest.getPassword(), userEmailAuthEntity.getPassword())) {
            throw new RuntimeException("invalid credentials");
        }
        redisTemplate.delete(KeyUtil.generateLogoutKey(userEntity.getId()));

        String jwtToken = jwtService.generateToken(userEntity.getId(), userEmailAuthEntity.getEmail());
        Token accessToken = new Token(TokenType.ACCESS, jwtToken, null);
        Token refreshToken = tokenService.generateRefreshToken(userEntity.getId());
        return new UserLoginResponse(userEntity.getUserName(), userEmailAuthEntity.getEmail(), userEntity.getId(),
                accessToken, refreshToken);
    }

    private void validateUser(UserEntity userEntity) {
        if (userEntity == null || !userEntity.getVerified()) {
            throw new RuntimeException("invalid user");
        }
    }

    public UserLogoutResponse logoutUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        log.info("logging out user {}", userId);
        redisTemplate.opsForValue().set(KeyUtil.generateLogoutKey(userId), String.valueOf(new Date().getTime()), 1, TimeUnit.HOURS);
        long revokedAt = tokenService.revokeRefreshToken(userId);
        return new UserLogoutResponse(userId, true, revokedAt);
    }

    public UserRefreshSessionResponse refreshSession(UserRefreshSessionRequest userRefreshSessionRequest) {
        String refreshToken = userRefreshSessionRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("invalid refresh token");
        }
        Long userId = tokenService.validateRefreshToken(userRefreshSessionRequest.getRefreshToken());
        UserEmailAuthEntity userEmailAuthEntity = userEmailAuthRepository.findByUserId(userId);
        String jwtToken = jwtService.generateToken(userId, userEmailAuthEntity.getEmail());
        return new UserRefreshSessionResponse(new Token(TokenType.ACCESS, jwtToken, null));
    }

    public UserDeleteAccountResponse deleteUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        logoutUser();
        UserEntity userEntity = userRepository.findByIdAndDeletedFalse(userId).orElseThrow();
        userEntity.setDeleted(true);
        userRepository.save(userEntity);
        return new UserDeleteAccountResponse(userId, true);
    }
}
