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
@Table(name = "user_email_auths")
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailAuthEntity extends BaseEntity {
    private Long userId;
    private String email;
    private String password;
}
