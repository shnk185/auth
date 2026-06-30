package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users/sessions")
public class UserSessionController {

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping
    public void loginUser(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse httpServletResponse) throws IOException {
        log.info("user login request {}", userLoginRequest);
        userAuthService.loginUser(userLoginRequest, httpServletResponse);
        log.info("user login response {}", httpServletResponse);
    }

    @DeleteMapping
    public void logoutUser(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        log.info("user logout request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        userAuthService.logoutUser(httpServletResponse);
        log.info("user logout response {}", httpServletResponse);
    }

    @PostMapping("refresh")
    public void refreshSession(UserRefreshSessionRequest userRefreshSessionRequest, HttpServletResponse httpServletResponse) {
        log.info("user refresh session request {}", userRefreshSessionRequest);
        userAuthService.refreshSession(userRefreshSessionRequest, httpServletResponse);
        log.info("user refresh session response {}", httpServletResponse);
    }
}
