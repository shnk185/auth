package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.request.UserRefreshSessionRequest;
import org.ngs.auth.dto.response.UserLogoutResponse;
import org.ngs.auth.dto.response.UserRefreshSessionResponse;
import org.ngs.auth.service.UserAuthService;
import org.ngs.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users/sessions")
public class UserSessionController {

    @Autowired
    private UserAuthService userAuthService;


    @DeleteMapping
    public void logoutUser(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        log.info("user logout request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        UserLogoutResponse response = userAuthService.logoutUser();
        CookieUtil.removeAuthCookies(httpServletResponse);
        httpServletResponse.sendRedirect("/");
        log.info("user logout response {}", response);
    }

    @PostMapping("refresh")
    public void refreshSession(UserRefreshSessionRequest userRefreshSessionRequest, HttpServletResponse httpServletResponse) {
        log.info("user refresh session request {}", userRefreshSessionRequest);
        UserRefreshSessionResponse response = userAuthService.refreshSession(userRefreshSessionRequest);
        CookieUtil.setAccessAndRefreshTokenCookiesInResponse(response.getAccessToken(), response.getRefreshToken(), httpServletResponse);
        log.info("user refresh session response {}", response);
    }
}
