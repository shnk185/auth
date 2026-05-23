package org.ngs.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.config.ZaloAuthConfig;
import org.ngs.auth.constant.Constants;
import org.ngs.auth.constant.ZaloConstants;
import org.ngs.auth.dto.UserLoginResponse;
import org.ngs.auth.dto.external.ZaloAccessCodeResponse;
import org.ngs.auth.dto.external.ZaloSocialResponse;
import org.ngs.auth.service.external.zalo.ZaloAccessCodeService;
import org.ngs.auth.service.external.zalo.ZaloSocialService;
import org.ngs.auth.util.KeyUtil;
import org.ngs.auth.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserZaloAuthService {

    @Autowired
    private ZaloAuthConfig zaloAuthConfig;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ZaloAccessCodeService zaloAccessCodeService;

    @Autowired
    private ZaloSocialService zaloSocialService;

    public RedirectView redirectToZalo() {
        String codeVerifierToken = generateCodeVerifier();
        String loginUUID = UUID.randomUUID().toString();
        String zaloRedirectUrl = generateZaloRedirectUrl(loginUUID, TokenUtil.hashToken(codeVerifierToken));
        redisTemplate.opsForValue().set(KeyUtil.generateZaloCodeVerifierKey(loginUUID), codeVerifierToken,
                zaloAuthConfig.getCodeVerifierTimeMs(), TimeUnit.MILLISECONDS);
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

    public UserLoginResponse loginWithZalo(String oauthCode, String loginUUID, String codeChallenge) {
        String codeVerifierToken = redisTemplate.opsForValue().get(KeyUtil.generateZaloCodeVerifierKey(loginUUID));
        if (codeVerifierToken == null) {
            throw new RuntimeException("zalo login expired");
        }
        ZaloAccessCodeResponse zaloAccessCodeResponse = zaloAccessCodeService.fetchAccessTokenFromCode(oauthCode, codeVerifierToken);
        ZaloSocialResponse zaloSocialResponse = zaloSocialService.fetchSocialResponse(zaloAccessCodeResponse.getAccessToken(),
                Arrays.asList("id", "name"));
        return new UserLoginResponse();
    }



}
