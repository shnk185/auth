package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.config.ZaloAuthConfig;
import org.ngs.auth.constant.Constants;
import org.ngs.auth.constant.ZaloConstants;
import org.ngs.auth.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
public class UserZaloAuthService {

    @Autowired
    private ZaloAuthConfig zaloAuthConfig;

    @Autowired
    private SecureRandom secureRandom;

    public RedirectView redirectToZalo() {
        String codeVerifier = generateCodeVerifier();
        String tokenHash = TokenUtil.hashToken(codeVerifier);
        String loginUUID = UUID.randomUUID().toString();
        String zaloRedirectUrl = generateZaloRedirectUrl(loginUUID, tokenHash);
        return new RedirectView(zaloRedirectUrl);
    }

    private String generateZaloRedirectUrl(String loginUUID, String tokenHash) {
        String redirectUri = UriComponentsBuilder.fromUriString(zaloAuthConfig.getAuthCodeUrl())
                .queryParam(ZaloConstants.APP_ID, zaloAuthConfig.getAppId())
                .queryParam(ZaloConstants.REDIRECT_URI, zaloAuthConfig.getRedirectUri())
                .queryParam(ZaloConstants.STATE, loginUUID)
                .queryParam(ZaloConstants.CODE_CHALLENGE, tokenHash)
                .toUriString();
        log.info("generated redirect uri {}", redirectUri);
        return redirectUri;
    }

    private String generateCodeVerifier() {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < 43; index++) {
            sb.append(Constants.ALPHA_NUMERIC.charAt(secureRandom.nextInt(0, Constants.ALPHA_NUMERIC.length())));
        }
        String codeVerifier = sb.toString();
        log.info("generated zalo code verifier {}", codeVerifier);
        return codeVerifier;
    }
}
