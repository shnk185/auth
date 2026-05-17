package org.ngs.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.enums.TokenType;

import java.util.Date;

@Data
@Entity
@Builder
@Table(name = "user_token_mappings")
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenMappingEntity extends BaseEntity {
    private Long userId;
    private String tokenHash;
    @Column(name = "token_type", columnDefinition = "varchar(128)")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    private Date expiresAt;
    private Long revokedAt;
}
