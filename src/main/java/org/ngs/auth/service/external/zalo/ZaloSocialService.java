package org.ngs.auth.service.external.zalo;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.config.ZaloAuthConfig;
import org.ngs.auth.constant.DelimiterConstants;
import org.ngs.auth.constant.ZaloConstants;
import org.ngs.auth.dto.external.ZaloSocialResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
public class ZaloSocialService {


    @Autowired
    @Qualifier("proxyRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private ZaloAuthConfig zaloAuthConfig;

    @Autowired
    private ObjectMapper objectMapper;

    public ZaloSocialResponse fetchSocialResponse(String accessToken, List<String> fields) {
        String url = generateZaloSocialApiUrl(fields);
        HttpHeaders httpHeaders = generateZaloSocialApiHeaders(accessToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String rawResponse = response.getBody();
                log.info("received raw zalo social api response {}", rawResponse);
                ZaloSocialResponse zaloSocialResponse = objectMapper.readValue(rawResponse, ZaloSocialResponse.class);
                log.info("received zalo social api response {}", zaloSocialResponse);
                return zaloSocialResponse;
            } else {
                log.error("received non 2xx response on fetch zalo social api response");
                throw new RuntimeException("received non 2xx response on fetch zalo social api response");
            }
        } catch (Exception e) {
            log.error("failed to request zalo social api response", e);
            throw new RuntimeException(e);
        }
    }

    private String generateZaloSocialApiUrl(List<String> fields) {
        String zaloSocialApiUrl = UriComponentsBuilder.fromUriString(zaloAuthConfig.getSocialApiUrl())
                .queryParam(ZaloConstants.FIELDS, String.join(DelimiterConstants.COMMA, fields))
                .toUriString();
        log.info("zalo social api url {}", zaloSocialApiUrl);
        return zaloSocialApiUrl;
    }

    private HttpHeaders generateZaloSocialApiHeaders(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(ZaloConstants.ACCESS_TOKEN, accessToken);
        return httpHeaders;
    }

}
