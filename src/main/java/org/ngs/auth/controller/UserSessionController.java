package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.request.UserLoginRequest;
import org.ngs.auth.dto.response.UserLoginResponse;
import org.ngs.auth.dto.response.UserLogoutResponse;
import org.ngs.auth.dto.request.UserRefreshSessionRequest;
import org.ngs.auth.dto.response.UserRefreshSessionResponse;
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
@RequestMapping("/users/sessions")
public class UserSessionController {

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping
    public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        log.info("user login request {}", userLoginRequest);
        UserLoginResponse response = userAuthService.loginUser(userLoginRequest);
        log.info("user login response {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<UserLogoutResponse> logoutUser(HttpServletRequest request) {
        log.info("user logout request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        UserLogoutResponse response = userAuthService.logoutUser();
        log.info("user logout response {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("refresh")
    public ResponseEntity<UserRefreshSessionResponse> refreshSession(UserRefreshSessionRequest userRefreshSessionRequest) {
        log.info("user refresh session request {}", userRefreshSessionRequest);
        UserRefreshSessionResponse response = userAuthService.refreshSession(userRefreshSessionRequest);
        log.info("user refresh session response {}", response);
        return ResponseEntity.ok(response);
    }
}
