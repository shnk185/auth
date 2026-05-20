package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.*;
import org.ngs.auth.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/sessions")
    public ResponseEntity<UserLogoutResponse> logoutUser(HttpServletRequest request) {
        log.info("user logout request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        UserLogoutResponse response = userAuthService.logoutUser();
        log.info("user logout response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/refresh")
    public ResponseEntity<UserRefreshSessionResponse> refreshSession(UserRefreshSessionRequest userRefreshSessionRequest) {
        log.info("user refresh session request {}", userRefreshSessionRequest);
        UserRefreshSessionResponse response = userAuthService.refreshSession(userRefreshSessionRequest);
        log.info("user refresh session response {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<UserDeleteAccountResponse> deleteAccount(HttpServletRequest request) {
        log.info("user delete account request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        UserDeleteAccountResponse response = userAuthService.deleteUser();
        log.info("user delete account response {}", response);
        return ResponseEntity.ok(response);
    }
}
