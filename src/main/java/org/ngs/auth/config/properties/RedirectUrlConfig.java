package org.ngs.auth.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redirect.url")
public class RedirectUrlConfig {
    private String problemSetPage;
    private String homePage;
}
