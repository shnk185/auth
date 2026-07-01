package org.ngs.auth.service;

import jakarta.transaction.Transactional;
import org.ngs.auth.dto.Token;
import org.ngs.auth.dto.request.UserCreateRequest;
import org.ngs.auth.dto.request.UserLoginRequest;
import org.ngs.auth.dto.request.UserVerifyRequest;
import org.ngs.auth.dto.response.UserCreateResponse;
import org.ngs.auth.dto.response.UserLoginResponse;
import org.ngs.auth.dto.response.UserVerificationResponse;
import org.ngs.auth.entity.UserEmailAuthEntity;
import org.ngs.auth.entity.UserEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.repository.UserEmailAuthRepository;
import org.ngs.auth.repository.UserRepository;
import org.ngs.auth.util.KeyUtil;
import org.ngs.auth.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class UserEmailAuthService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEmailAuthRepository userEmailAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenService tokenService;

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
        redisTemplate.delete(RedisKeyUtil.generateLogoutKey(userEntity.getId()));
        Token accessToken = jwtService.generateToken(userEntity.getId(), userEmailAuthEntity.getEmail());
        Token refreshToken = tokenService.generateRefreshToken(userEntity.getId());
        return new UserLoginResponse(userEntity.getUserName(), userEmailAuthEntity.getEmail(), userEntity.getId(),
                accessToken, refreshToken);
    }

    private void validateUser(UserEntity userEntity) {
        if (userEntity == null || !userEntity.getVerified()) {
            throw new RuntimeException("invalid user");
        }
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

    private void validateUserCreateRequest(UserCreateRequest userCreateRequest) {
        if (userRepository.findByUserNameAndDeletedFalse(userCreateRequest.getUserName()) != null) {
            throw new RuntimeException("user already exists");
        } else if (userEmailAuthRepository.findByEmail(userCreateRequest.getEmail()) != null) {
            throw new RuntimeException("user email already exists");
        }
    }
}
