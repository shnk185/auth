package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.request.UserCreateRequest;
import org.ngs.auth.dto.request.UserVerifyRequest;
import org.ngs.auth.dto.response.UserCreateResponse;
import org.ngs.auth.dto.response.UserVerificationResponse;
import org.ngs.auth.service.UserAuthService;
import org.ngs.auth.service.UserEmailAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private UserEmailAuthService userEmailAuthService;


    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        log.info("user create request {}", userCreateRequest);
        UserCreateResponse response = userEmailAuthService.createUser(userCreateRequest);
        log.info("user create response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<UserVerificationResponse> verifyUser(@RequestBody UserVerifyRequest userVerifyRequest) {
        log.info("user verify request {}", userVerifyRequest);
        UserVerificationResponse response = userEmailAuthService.verifyUser(userVerifyRequest);
        log.info("user verify response {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public void deleteAccount(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        log.info("user delete account request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        userAuthService.deleteUser(httpServletResponse);
        log.info("user delete account response {}", httpServletResponse);
    }
}