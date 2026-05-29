package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.UserLoginResponse;
import org.ngs.auth.service.UserZaloAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/users/sessions/oauth")
public class UserOAuthController {

    @Autowired
    private UserZaloAuthService userZaloAuthService;

    @GetMapping("/zalo")
    public RedirectView loginWithZalo(HttpServletRequest request) {
        log.info("user login with zalo request {}", request.getHeader(HttpHeaders.AUTHORIZATION));
        RedirectView response = userZaloAuthService.redirectToZalo();
        log.info("user login with zalo response {}", response);
        return response;
    }

    @PostMapping("/zalo/callback")
    public ResponseEntity<UserLoginResponse> zaloCallBack(@RequestParam("code") String oauthCode,
                                                          @RequestParam("state") String loginUUID,
                                                          @RequestParam("code_challenge") String codeChallenge) {
        log.info("user login with zalo callback code {} state {} codeChallenge {}", oauthCode, loginUUID, codeChallenge);
        UserLoginResponse response = userZaloAuthService.handleZaloCallback(oauthCode, loginUUID, codeChallenge);
        log.info("user login with zalo callback response {}", response);
        return ResponseEntity.ok(response);
    }

}
