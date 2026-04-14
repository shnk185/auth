package org.ngs.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.dto.ServiceAuthConfig;
import org.ngs.auth.service.ServiceAuthConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ServiceAuthConfigService serviceAuthConfigService;

    @PostMapping
    public ResponseEntity<ServiceAuthConfig> createServiceAuthConfig(@RequestBody ServiceAuthConfig serviceAuthConfig) {
        log.info("service auth config create request {}", serviceAuthConfig);
        ServiceAuthConfig response = serviceAuthConfigService.createServiceAuthConfig(serviceAuthConfig);
        log.info("service auth config create response {}", response);
        return ResponseEntity.ok(response);
    }
}
