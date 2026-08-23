package org.ngs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.config.properties.RedirectUrlConfig;
import org.ngs.auth.dto.response.UserZaloCallbackResponse;
import org.ngs.auth.service.UserZaloAuthService;
import org.ngs.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/users/sessions/oauth")
public class UserOAuthZaloController {

    @Autowired
    private UserZaloAuthService userZaloAuthService;

    @Autowired
    private RedirectUrlConfig redirectUrlConfig;

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
        UserZaloCallbackResponse response = userZaloAuthService.handleZaloCallback(oauthCode, loginUUID, codeChallenge, httpServletResponse);
        CookieUtil.setAccessAndRefreshTokenCookiesInResponse(response.getAccessToken(), response.getRefreshToken(),
                httpServletResponse);
        httpServletResponse.sendRedirect(redirectUrlConfig.getProblemSetPage());
        log.info("user login with zalo callback successful {}", httpServletResponse);
    }
}
