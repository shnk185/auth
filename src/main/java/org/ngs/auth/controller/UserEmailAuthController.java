package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.request.UserLoginRequest;
import org.ngs.auth.dto.response.UserLoginResponse;
import org.ngs.auth.service.UserEmailAuthService;
import org.ngs.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users/sessions/email")
public class UserEmailAuthController {

    @Autowired
    private UserEmailAuthService userEmailAuthService;

    @PostMapping
    public void loginUser(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse httpServletResponse) throws IOException {
        log.info("user login request {}", userLoginRequest);
        UserLoginResponse response = userEmailAuthService.loginUser(userLoginRequest);
        CookieUtil.setAccessAndRefreshTokenCookiesInResponse(response.getAccessToken(), response.getRefreshToken(),
                httpServletResponse);
        httpServletResponse.sendRedirect("/");
        log.info("user login response {}", httpServletResponse);
    }
}
