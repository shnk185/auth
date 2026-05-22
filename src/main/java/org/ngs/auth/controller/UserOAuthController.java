package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.service.UserZaloAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/users/sessions/oauth")
public class UserOAuthController {

    @Autowired
    private UserZaloAuthService userZaloAuthService;

    @PostMapping("/zalo")
    public RedirectView loginWithZalo(HttpServletRequest request) {
        log.info("user login with zalo request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        RedirectView response = userZaloAuthService.redirectToZalo();
        log.info("user login with zalo response {}", response);
        return response;
    }
}
