package org.ngs.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVerifyRequest extends BaseRequest {
    private String verificationCode;
    private Long userId;
}
