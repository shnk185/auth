package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.service.UserZaloAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users/sessions/oauth")
public class UserOAuthZaloController {

    @Autowired
    private UserZaloAuthService userZaloAuthService;

    @GetMapping("/zalo")
    public RedirectView loginWithZalo(HttpServletRequest request) {
        log.info("user login with zalo request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        RedirectView response = userZaloAuthService.redirectToZalo();
        log.info("user login with zalo response {}", response);
        return response;
    }

    @GetMapping("/zalo/callback")
    public void zaloCallBack(@RequestParam("code") String oauthCode,
                                                          @RequestParam("state") String loginUUID,
                                                          @RequestParam("code_challenge") String codeChallenge,
                                                          HttpServletResponse httpServletResponse) throws IOException {
        log.info("user login with zalo callback code {} state {} codeChallenge {}", oauthCode, loginUUID, codeChallenge);
        userZaloAuthService.handleZaloCallback(oauthCode, loginUUID, codeChallenge, httpServletResponse);
        log.info("user login with zalo callback successful {}", httpServletResponse);
    }

}
