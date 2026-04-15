package org.ngs.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.ngs.auth.constant.DelimiterConstants;
import org.ngs.auth.constant.KeyConstants;

@Slf4j
public class KeyUtil {

    public static String generateSignUpVerifyKey(Long userId) {
        String key = String.join(DelimiterConstants.UNDERSCORE, KeyConstants.AUTH_SERVICE, KeyConstants.VERIFICATION_CODE,
                KeyConstants.USER_ID, String.valueOf(userId));
        log.info("generated sign up verify key: {}", key);
        return key;
    }
}
