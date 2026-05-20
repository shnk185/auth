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
        ServiceAuthConfigEntity serviceAuthConfigEntity = new ServiceAuthConfigEntity(serviceAuthConfig.getName(),
                serviceAuthConfig.getContextPath());
        serviceAuthConfigRepository.save(serviceAuthConfigEntity);

        List<AuthMethod> signUpMethods = createSignUpMethods(serviceAuthConfig, serviceAuthConfigEntity);
        List<AuthMethod> loginMethods = createLogInMethods(serviceAuthConfig, serviceAuthConfigEntity);

        return new ServiceAuthConfig(serviceAuthConfigEntity.getName(), serviceAuthConfigEntity.getContextPath(),
                signUpMethods, loginMethods);
    }

    private List<AuthMethod> createLogInMethods(ServiceAuthConfig serviceAuthConfig, ServiceAuthConfigEntity serviceAuthConfigEntity) {
        List<AuthMethod> loginMethods = new ArrayList<>();
        if (serviceAuthConfig.getLoginMethods() == null) {
            return loginMethods;
        }
        for (AuthMethod authMethod: serviceAuthConfig.getLoginMethods()) {
            ServiceAuthConfigLogInMethodEntity serviceAuthConfigLogInMethodEntity = new ServiceAuthConfigLogInMethodEntity(
                    serviceAuthConfigEntity.getId(), authMethod);
            serviceAuthConfigLogInMethodRepository.save(serviceAuthConfigLogInMethodEntity);
            loginMethods.add(serviceAuthConfigLogInMethodEntity.getAuthMethod());
        }
        return loginMethods;
    }

    private List<AuthMethod> createSignUpMethods(ServiceAuthConfig serviceAuthConfig, ServiceAuthConfigEntity serviceAuthConfigEntity) {
        List<AuthMethod> signUpMethods = new ArrayList<>();
        if (serviceAuthConfig.getSignUpMethods() == null) {
            return signUpMethods;
        }
        for (AuthMethod authMethod: serviceAuthConfig.getSignUpMethods()) {
            ServiceAuthConfigSignUpMethodEntity serviceAuthConfigSignUpMethodEntity = new ServiceAuthConfigSignUpMethodEntity(
                    serviceAuthConfigEntity.getId(), authMethod);
            serviceAuthConfigSignUpMethodRepository.save(serviceAuthConfigSignUpMethodEntity);
            signUpMethods.add(serviceAuthConfigSignUpMethodEntity.getAuthMethod());
        }
        return signUpMethods;
    }
}
