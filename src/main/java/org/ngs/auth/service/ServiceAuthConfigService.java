package org.ngs.auth.service;

import jakarta.transaction.Transactional;
import org.ngs.auth.dto.ServiceAuthConfig;
import org.ngs.auth.entity.ServiceAuthConfigEntity;
import org.ngs.auth.entity.ServiceAuthConfigLogInMethodEntity;
import org.ngs.auth.entity.ServiceAuthConfigSignUpMethodEntity;
import org.ngs.auth.enums.AuthMethod;
import org.ngs.auth.repository.ServiceAuthConfigLogInMethodRepository;
import org.ngs.auth.repository.ServiceAuthConfigRepository;
import org.ngs.auth.repository.ServiceAuthConfigSignUpMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceAuthConfigService {

    @Autowired
    private ServiceAuthConfigRepository serviceAuthConfigRepository;

    @Autowired
    private ServiceAuthConfigSignUpMethodRepository serviceAuthConfigSignUpMethodRepository;

    @Autowired
    private ServiceAuthConfigLogInMethodRepository serviceAuthConfigLogInMethodRepository;

    @Transactional
    public ServiceAuthConfig createServiceAuthConfig(ServiceAuthConfig serviceAuthConfig) {
        ServiceAuthConfigEntity serviceAuthConfigEntity = ServiceAuthConfigEntity.builder()
                .name(serviceAuthConfig.getName())
                .contextPath(serviceAuthConfig.getContextPath())
                .build();
        serviceAuthConfigRepository.save(serviceAuthConfigEntity);

        List<AuthMethod> signUpMethods = createSignUpMethods(serviceAuthConfig, serviceAuthConfigEntity);
        List<AuthMethod> loginMethods = createLogInMethods(serviceAuthConfig, serviceAuthConfigEntity);

        return ServiceAuthConfig.builder()
                .name(serviceAuthConfigEntity.getName())
                .contextPath(serviceAuthConfigEntity.getContextPath())
                .loginMethods(loginMethods)
                .signUpMethods(signUpMethods)
                .build();
    }

    private List<AuthMethod> createLogInMethods(ServiceAuthConfig serviceAuthConfig, ServiceAuthConfigEntity serviceAuthConfigEntity) {
        List<AuthMethod> loginMethods = new ArrayList<>();
        if (serviceAuthConfig.getLoginMethods() != null) {
            for (AuthMethod authMethod: serviceAuthConfig.getLoginMethods()) {
                ServiceAuthConfigLogInMethodEntity serviceAuthConfigLogInMethodEntity = ServiceAuthConfigLogInMethodEntity
                        .builder()
                        .serviceAuthConfigId(serviceAuthConfigEntity.getId())
                        .authMethod(authMethod)
                        .build();
                serviceAuthConfigLogInMethodRepository.save(serviceAuthConfigLogInMethodEntity);
                loginMethods.add(serviceAuthConfigLogInMethodEntity.getAuthMethod());
            }
        }
        return loginMethods;
    }

    private List<AuthMethod> createSignUpMethods(ServiceAuthConfig serviceAuthConfig, ServiceAuthConfigEntity serviceAuthConfigEntity) {
        List<AuthMethod> signUpMethods = new ArrayList<>();
        if (serviceAuthConfig.getSignUpMethods() != null) {
            for (AuthMethod authMethod: serviceAuthConfig.getSignUpMethods()) {
                ServiceAuthConfigSignUpMethodEntity serviceAuthConfigSignUpMethodEntity = ServiceAuthConfigSignUpMethodEntity
                        .builder()
                        .serviceAuthConfigId(serviceAuthConfigEntity.getId())
                        .authMethod(authMethod)
                        .build();
                serviceAuthConfigSignUpMethodRepository.save(serviceAuthConfigSignUpMethodEntity);
                signUpMethods.add(serviceAuthConfigSignUpMethodEntity.getAuthMethod());
            }
        }
        return signUpMethods;
    }
}
