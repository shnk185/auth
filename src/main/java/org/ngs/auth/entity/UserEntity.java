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
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity {
    private String userName;
    @Column(name = "auth_method", columnDefinition = "varchar(128)")
    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod;
    private Boolean verified;
}
