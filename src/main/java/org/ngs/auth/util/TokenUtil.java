package org.ngs.auth.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class TokenUtil {

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String res = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            log.info("generated token hash {}", res);
            return res;
        } catch (NoSuchAlgorithmException e) {
            log.error("hash algorithm not found");
            throw new RuntimeException("hash algorithm not found");
        }
    }
}
