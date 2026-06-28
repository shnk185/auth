package org.ngs.auth.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zalo.auth")
public class ZaloAuthConfig {
    private String authCodeUrl;
    private String appId;
    private String redirectUri;
    private Integer codeVerifierTimeMs;
    private String accessTokenUrl;
    private String socialApiUrl;
    private String khoaBiMatCuaUngDung;
}
