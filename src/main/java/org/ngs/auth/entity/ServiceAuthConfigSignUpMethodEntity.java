package org.ngs.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.enums.AuthMethod;

@Data
@Entity
@Builder
@Table(name = "service_auth_config_sign_up_methods")
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAuthConfigSignUpMethodEntity extends BaseEntity {
    private Long serviceAuthConfigId;

    @Column(name = "auth_method", columnDefinition = "varchar(128)")
    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod;
}
