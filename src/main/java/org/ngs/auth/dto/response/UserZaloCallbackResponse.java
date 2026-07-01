package org.ngs.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.auth.dto.Token;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserZaloCallbackResponse {
    private Token accessToken;
    private Token refreshToken;
}
