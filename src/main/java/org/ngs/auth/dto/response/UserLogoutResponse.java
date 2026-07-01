package org.ngs.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLogoutResponse {
    private Long userId;
    private boolean loggedOut;
    private long loggedOutAt;
}
