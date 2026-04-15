package org.ngs.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.enums.AuthMethod;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateResponse {
    private Long userId;
    private String userName;
    private AuthMethod authMethod;
    private Boolean verified;
    private Boolean deleted;
    private String email;
}
