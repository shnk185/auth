package org.ngs.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.ngs.auth.constant.CookieConstants;
import org.ngs.auth.dto.Token;

public class CookieUtil {

    public static void setAccessAndRefreshTokenCookiesInResponse(Token accessToken, Token refreshToken, HttpServletResponse httpServletResponse) {
        Cookie accessTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.ACCESS_TOKEN, accessToken);
        httpServletResponse.addCookie(accessTokenCookie);
        Cookie refreshTokenCookie = CookieUtil.generateTokenCookie(CookieConstants.REFRESH_TOKEN, refreshToken);
        httpServletResponse.addCookie(refreshTokenCookie);
    }

    public static void removeAuthCookies(HttpServletResponse response) {
        CookieUtil.removeCookie(CookieConstants.ACCESS_TOKEN, response);
        CookieUtil.removeCookie(CookieConstants.REFRESH_TOKEN, response);
    }

    public static void removeCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public static Cookie generateTokenCookie(String tokenType, Token accessToken) {
        Cookie cookie = new Cookie(tokenType, accessToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setMaxAge(Math.toIntExact(accessToken.getMaxAge()));
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
