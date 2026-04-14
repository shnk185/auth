package org.ngs.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.enums.AuthMethod;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAuthConfig extends BaseRequest {
    private String name;
    private String contextPath;
    private List<AuthMethod> signUpMethods;
    private List<AuthMethod> loginMethods;
}
