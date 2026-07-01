package org.ngs.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.dto.Token;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRefreshSessionResponse {
    private Token accessToken;
    private Token refreshToken;
}
