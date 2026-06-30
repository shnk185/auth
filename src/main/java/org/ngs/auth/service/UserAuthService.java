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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailAuthRepository userEmailAuthRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Transactional
    public UserCreateResponse createUser(UserCreateRequest userCreateRequest) {
        validateUserCreateRequest(userCreateRequest);
        UserEntity userEntity = new UserEntity(userCreateRequest.getUserName(),  AuthMethod.EMAIL, false);
        userRepository.save(userEntity);
        UserEmailAuthEntity userEmailAuthEntity = new UserEmailAuthEntity(userEntity.getId(), userCreateRequest.getEmail(),
                passwordEncoder.encode(userCreateRequest.getPassword()));
        userEmailAuthRepository.save(userEmailAuthEntity);

        String otp = KeyUtil.generateSixDigitOtp(secureRandom);
        redisTemplate.opsForValue().set(RedisKeyUtil.generateSignUpVerifyKey(userEntity.getId()), otp, 24, TimeUnit.HOURS);
        emailSenderService.sendSignUpOtpEmail(userEmailAuthEntity.getEmail(), otp);
        return new UserCreateResponse(userEntity.getId(), userEntity.getUserName(), userEntity.getAuthMethod(),
                userEntity.getVerified(), userEntity.isDeleted(), userEmailAuthEntity.getEmail());
    }

    private void validateUserCreateRequest(UserCreateRequest userCreateRequest) {
        if (userRepository.findByUserNameAndDeletedFalse(userCreateRequest.getUserName()) != null) {
            throw new RuntimeException("user already exists");
        } else if (userEmailAuthRepository.findByEmail(userCreateRequest.getEmail()) != null) {
            throw new RuntimeException("user email already exists");
        }
    }

    public UserVerificationResponse verifyUser(UserVerifyRequest userVerifyRequest) {
        Long userId = userVerifyRequest.getUserId();
        String otp = redisTemplate.opsForValue().get(RedisKeyUtil.generateSignUpVerifyKey(userId));
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
        String verificationAttemptKey = RedisKeyUtil.generateSignUpVerifyAttemptsKey(userVerifyRequest.getUserId());
        Long verificationAttempts = redisTemplate.opsForValue().decrement(verificationAttemptKey);
        if (verificationAttempts == -1) {
            redisTemplate.opsForValue().set(verificationAttemptKey, "4", 24, TimeUnit.HOURS);
            verificationAttempts = 4L;
        } else if (verificationAttempts == 0) {
            redisTemplate.delete(RedisKeyUtil.generateSignUpVerifyKey(userVerifyRequest.getUserId()));
        }
        return verificationAttempts;
    }

    public void loginUser(UserLoginRequest userLoginRequest, HttpServletResponse httpServletResponse) throws IOException {
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
        redisTemplate.delete(RedisKeyUtil.generateLogoutKey(userEntity.getId()));
        cookieService.setAccessAndRefreshTokenCookiesInResponse(userEntity.getId(), userEmailAuthEntity.getEmail(),
                httpServletResponse);
        httpServletResponse.sendRedirect("/");
    }

    private void validateUser(UserEntity userEntity) {
        if (userEntity == null || !userEntity.getVerified()) {
            throw new RuntimeException("invalid user");
        }
    }

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
