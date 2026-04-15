package org.ngs.auth.service;

import jakarta.transaction.Transactional;
import org.ngs.auth.dto.UserCreateRequest;
import org.ngs.auth.dto.UserCreateResponse;
import org.ngs.auth.entity.UserEmailAuthEntity;
import org.ngs.auth.entity.UserEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.repository.UserEmailAuthRepository;
import org.ngs.auth.repository.UserRepository;
import org.ngs.auth.util.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
}
