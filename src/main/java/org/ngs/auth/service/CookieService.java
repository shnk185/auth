package org.ngs.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.ngs.auth.constant.CookieConstants;
import org.ngs.auth.dto.Token;
import org.ngs.auth.util.CookieUtil;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public void setAccessAndRefreshTokenCookiesInResponse(Token accessToken, Token refreshToken, HttpServletResponse httpServletResponse) {
        Cookie accessTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.ACCESS_TOKEN, accessToken);
        httpServletResponse.addCookie(accessTokenCookie);
        Cookie refreshTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.REFRESH_TOKEN, refreshToken);
        httpServletResponse.addCookie(refreshTokenCookie);
    }
}
