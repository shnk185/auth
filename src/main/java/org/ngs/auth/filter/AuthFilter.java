package org.ngs.auth.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.constant.CookieConstants;
import org.ngs.auth.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public AuthFilter(@Value("${app.jwt.secret}") String jwtSecret) {
        byte[] decoded = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(decoded);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<Cookie> accessTokenOptional = Stream.ofNullable(request.getCookies()).flatMap(Arrays::stream)
                .filter(x -> x.getName().equals(CookieConstants.ACCESS_TOKEN)).findFirst();
        if (accessTokenOptional.isPresent()) {
            try {
                Cookie accessTokenCookie = accessTokenOptional.get();
                log.info("extracted access token cookie {}", accessTokenCookie);
                String token = accessTokenCookie.getValue();

                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                Date expiresAt = claims.getExpiration();
                Date now = new Date();
                Long userId = Long.parseLong(claims.getSubject());
                String logoutEpoch = redisTemplate.opsForValue().get(RedisKeyUtil.generateLogoutKey(userId));
                if (expiresAt.before(now) || logoutEpoch != null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.error("Auth validation failed ", e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
