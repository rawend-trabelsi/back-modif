package com.rawend.demo.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}
