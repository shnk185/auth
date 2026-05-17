package org.ngs.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.*;
import org.ngs.auth.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        log.info("user create request {}", userCreateRequest);
        UserCreateResponse response = userAuthService.createUser(userCreateRequest);
        log.info("user create response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<UserVerificationResponse> verifyUser(@RequestBody UserVerifyRequest userVerifyRequest) {
        log.info("user verify request {}", userVerifyRequest);
        UserVerificationResponse response = userAuthService.verifyUser(userVerifyRequest);
        log.info("user verify response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions")
    public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        log.info("user login request {}", userLoginRequest);
        UserLoginResponse response = userAuthService.loginUser(userLoginRequest);
        log.info("user login response {}", response);
        return ResponseEntity.ok(response);
    }
}
