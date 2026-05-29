package org.ngs.auth.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class KeyUtil {

    public static String generateSixDigitOtp(SecureRandom secureRandom) {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < 6; index++) {
            sb.append(secureRandom.nextInt(10));
        }
        String otp = sb.toString();
        log.info("generated login otp {}", otp);
        return otp;
    }
}
