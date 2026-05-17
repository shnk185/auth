package org.ngs.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVerificationResponse {
    private Long userId;
    private boolean verified;
    private Long attemptsRemaining;
}
