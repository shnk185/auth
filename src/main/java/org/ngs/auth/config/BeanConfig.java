package org.ngs.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class BeanConfig {
    @Bean
    public PasswordEncoder createPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecureRandom createSecureRandom() {
        return new SecureRandom();
    }

    @Bean
    @Primary
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    @Bean("emailExecutorService")
    public ExecutorService createEmailExecutorService() {
        return Executors.newFixedThreadPool(10);
    }
}
