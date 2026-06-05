package org.ngs.auth.util;

import jakarta.servlet.http.Cookie;
import org.ngs.auth.dto.Token;

public class CookieUtil {

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
