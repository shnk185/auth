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
@Table(name = "user_zalo_auths")
@AllArgsConstructor
@NoArgsConstructor
public class UserZaloAuthEntity extends BaseEntity {
    private Long userId;

}
