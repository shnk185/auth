package org.ngs.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.ngs.auth.constant.CookieConstants;
import org.ngs.auth.dto.Token;
import org.ngs.auth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenService tokenService;

    public void setAccessAndRefreshTokenCookiesInResponse(Long userId, String email, HttpServletResponse httpServletResponse) {
        Token accessToken = jwtService.generateToken(userId, email);
        Cookie accessTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.ACCESS_TOKEN, accessToken);
        httpServletResponse.addCookie(accessTokenCookie);
        Token refreshToken = tokenService.generateRefreshToken(userId);
        Cookie refreshTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.REFRESH_TOKEN, refreshToken);
        httpServletResponse.addCookie(refreshTokenCookie);
    }
}
