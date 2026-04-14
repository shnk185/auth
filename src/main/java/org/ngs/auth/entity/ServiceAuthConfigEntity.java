package org.ngs.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "service_auth_configs")
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAuthConfigEntity extends BaseEntity {
    private String name;
    private String contextPath;
}
