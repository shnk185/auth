package org.ngs.auth.config.properties;

import lombok.Data;
import org.ngs.auth.enums.EmailType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "email.content.config")
public class EmailContentConfig {
    private Map<EmailType, String> subjectTemplates;
    private Map<EmailType, String> bodyTemplates;
}
