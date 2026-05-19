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
        UserEntity userEntity = UserEntity.builder()
                .userName(userCreateRequest.getUserName())
                .verified(false)
                .authMethod(AuthMethod.EMAIL)
                .build();

        userRepository.save(userEntity);

        UserEmailAuthEntity userEmailAuthEntity = UserEmailAuthEntity.builder()
                .email(userCreateRequest.getEmail())
                .password(passwordEncoder.encode(userCreateRequest.getPassword()))
                .userId(userEntity.getId())
                .build();

        userEmailAuthRepository.save(userEmailAuthEntity);

        redisTemplate.opsForValue().set(KeyUtil.generateSignUpVerifyKey(userEntity.getId()), "123456", 24, TimeUnit.HOURS);

        return UserCreateResponse.builder()
                .userName(userEntity.getUserName())
                .userId(userEntity.getId())
                .deleted(userEntity.isDeleted())
                .verified(userEntity.getVerified())
                .authMethod(userEntity.getAuthMethod())
                .email(userEmailAuthEntity.getEmail())
                .build();
    }

    public UserVerificationResponse verifyUser(UserVerifyRequest userVerifyRequest) {
        Long userId = userVerifyRequest.getUserId();
        String otp = redisTemplate.opsForValue().get(KeyUtil.generateSignUpVerifyKey(userId));
        if (otp == null) {
            throw new RuntimeException("verification failed");
        }


        boolean verified = otp.equals(userVerifyRequest.getVerificationCode());
        if (verified) {
            UserEntity userEntity = userRepository.findById(userId).orElseThrow();
            userEntity.setVerified(true);
            userRepository.save(userEntity);
            return UserVerificationResponse.builder()
                    .verified(true)
                    .userId(userId)
                    .build();
        }
        Long remainingAttempts = getRemainingAttempts(userVerifyRequest);

        return UserVerificationResponse.builder()
                .verified(false)
                .userId(userId)
                .attemptsRemaining(remainingAttempts)
                .build();

    }

    private Long getRemainingAttempts(UserVerifyRequest userVerifyRequest) {
        String verificationAttemptKey = KeyUtil.generateSignUpVerifyAttemptsKey(userVerifyRequest.getUserId());
        String verificationAttempts = redisTemplate.opsForValue().get(verificationAttemptKey);
        Long remainingAttempts = 4L;
        if (verificationAttempts == null) {
            redisTemplate.opsForValue().set(verificationAttemptKey, "4", 24, TimeUnit.HOURS);
        } else {
            remainingAttempts = redisTemplate.opsForValue().decrement(verificationAttemptKey);
            if (remainingAttempts == 0) {
                redisTemplate.delete(KeyUtil.generateSignUpVerifyKey(userVerifyRequest.getUserId()));
            }
        }
        return remainingAttempts;
    }

    public UserLoginResponse loginUser(UserLoginRequest userLoginRequest) {
        if (userLoginRequest.getUserName() == null && userLoginRequest.getEmail() == null) {
            throw new RuntimeException("invalid credentials");
        }

        UserEntity userEntity = null;
        UserEmailAuthEntity userEmailAuthEntity = null;
        if (userLoginRequest.getUserName() != null) {
            userEntity = userRepository.findByUserName(userLoginRequest.getUserName());
            validateUser(userEntity);
            userEmailAuthEntity = userEmailAuthRepository.findByUserId(userEntity.getId());
        } else {
            userEmailAuthEntity = userEmailAuthRepository.findByEmail(userLoginRequest.getEmail());
            if (userEmailAuthEntity == null) {
                throw new RuntimeException("invalid credentials");
            }
            userEntity = userRepository.findById(userEmailAuthEntity.getUserId()).orElseThrow();
            validateUser(userEntity);
        }

        if (!passwordEncoder.matches(userLoginRequest.getPassword(), userEmailAuthEntity.getPassword())) {
            throw new RuntimeException("invalid credentials");
        }
        redisTemplate.delete(KeyUtil.generateLogoutKey(userEntity.getId()));

        String jwtToken = jwtService.generateToken(userEntity.getId(), userEmailAuthEntity.getEmail());
        Token refreshToken = tokenService.generateRefreshToken(userEntity.getId());
        return UserLoginResponse.builder()
                .userId(userEntity.getId())
                .userName(userEntity.getUserName())
                .userName(userEmailAuthEntity.getEmail())
                .accessToken(Token.builder().token(jwtToken).tokenType(TokenType.ACCESS).build())
                .refreshToken(refreshToken)
                .build();
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

        return UserLogoutResponse.builder()
                .userId(userId)
                .loggedOut(true)
                .loggedOutAt(revokedAt)
                .build();
    }
}
