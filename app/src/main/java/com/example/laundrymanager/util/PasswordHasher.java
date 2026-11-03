package com.example.laundrymanager.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Băm mật khẩu SHA-256 kèm salt ngẫu nhiên, định dạng lưu trữ: {@code saltHex$hashHex}. */
public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final char SEPARATOR = '$';

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return toHex(salt) + SEPARATOR + toHex(digest(salt, password));
    }

    public static boolean matches(String password, String stored) {
        int separator = stored.indexOf(SEPARATOR);
        if (separator < 0) {
            return false;
        }
        byte[] salt = fromHex(stored.substring(0, separator));
        byte[] expected = fromHex(stored.substring(separator + 1));
        return MessageDigest.isEqual(digest(salt, password), expected);
    }

    private static byte[] digest(byte[] salt, String password) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(salt);
            return sha.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 không khả dụng", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            out.append(String.format("%02x", b));
        }
        return out.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }
}

