package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.UserCreateRequest;
import org.ngs.auth.dto.UserCreateResponse;
import org.ngs.auth.dto.UserDeleteAccountResponse;
import org.ngs.auth.dto.UserVerificationResponse;
import org.ngs.auth.dto.UserVerifyRequest;
import org.ngs.auth.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @DeleteMapping
    public ResponseEntity<UserDeleteAccountResponse> deleteAccount(HttpServletRequest request) {
        log.info("user delete account request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        UserDeleteAccountResponse response = userAuthService.deleteUser();
        log.info("user delete account response {}", response);
        return ResponseEntity.ok(response);
    }
}