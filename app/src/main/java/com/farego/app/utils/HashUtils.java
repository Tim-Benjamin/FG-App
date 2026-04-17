package com.farego.app.utils;
// ============================================================
// FILE: app/src/main/java/com/farego/app/utils/HashUtils.java
// PURPOSE: SHA-256 password hashing — no plaintext stored ever.
// ============================================================

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    private HashUtils() { /* utility class */ }

    /**
     * Returns the SHA-256 hex digest of the given input string.
     * Used for password storage and verification.
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}