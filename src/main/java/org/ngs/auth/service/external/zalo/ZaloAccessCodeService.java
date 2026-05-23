package org.ngs.auth.service.external.zalo;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.config.ZaloAuthConfig;
import org.ngs.auth.constant.ZaloConstants;
import org.ngs.auth.dto.external.ZaloAccessCodeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ZaloAccessCodeService {

    @Autowired
    private ZaloAuthConfig zaloAuthConfig;

    @Autowired
    private RestTemplate restTemplate;

    public ZaloAccessCodeResponse fetchAccessTokenFromCode(String oauthCode, String codeVerifierToken) {
        String url = zaloAuthConfig.getAccessTokenUrl();
        HttpHeaders httpHeaders = generateZaloAccessCodeHeaders();
        MultiValueMap<String, String> body = generateZaloAccessCodeBody(oauthCode, codeVerifierToken);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, httpHeaders);
        try {
            ResponseEntity<ZaloAccessCodeResponse> response = restTemplate.postForEntity(url, httpEntity, ZaloAccessCodeResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ZaloAccessCodeResponse zaloAccessCodeResponse = response.getBody();
                log.info("received zalo access code response {}", zaloAccessCodeResponse);
                return zaloAccessCodeResponse;
            } else {
                log.error("received non 2xx response on fetch zalo access token from code");
                throw new RuntimeException("received non 2xx response on fetch zalo access token from code");
            }
        } catch (Exception e) {
            log.error("failed to request zalo access token from auth code", e);
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders generateZaloAccessCodeHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(ZaloConstants.SECRET_KEY, zaloAuthConfig.getKhoaBiMatCuaUngDung());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return httpHeaders;
    }

    private MultiValueMap<String, String> generateZaloAccessCodeBody(String oauthCode, String codeVerifierToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set(ZaloConstants.CODE, oauthCode);
        body.set(ZaloConstants.APP_ID, zaloAuthConfig.getAppId());
        body.set(ZaloConstants.GRANT_TYPE, ZaloConstants.GRANT_TYPE_AUTHORIZATION_CODE);
        body.set(ZaloConstants.CODE_VERIFIER, codeVerifierToken);
        return body;
    }
}
