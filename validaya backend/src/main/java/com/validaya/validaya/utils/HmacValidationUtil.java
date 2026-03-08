package com.validaya.validaya.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for HMAC validation of webhook notifications.
 * Validates the integrity and authenticity of Stereum Pay webhook callbacks.
 */
@Slf4j
@Component
public class HmacValidationUtil {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Validates HMAC signature of webhook payload.
     *
     * @param payload the webhook payload body
     * @param receivedSignature the signature from x-signature header
     * @param secret the webhook secret
     * @return true if signature is valid, false otherwise
     */
    public static boolean validateSignature(String payload, String receivedSignature, String secret) {
        try {
            String calculatedSignature = calculateHmac(payload, secret);
            boolean isValid = constantTimeEquals(calculatedSignature, receivedSignature);
            
            if (!isValid) {
                log.warn("HMAC signature validation failed. Expected: {}, Received: {}", 
                        calculatedSignature, receivedSignature);
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error during HMAC validation: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates timestamp freshness to prevent replay attacks.
     *
     * @param receivedTimestamp the timestamp from x-timestamp header (seconds)
     * @param maxAgeSeconds maximum age in seconds (typically 5 minutes = 300 seconds)
     * @return true if timestamp is fresh, false if expired
     */
    public static boolean validateTimestamp(long receivedTimestamp, long maxAgeSeconds) {
        long currentTime = System.currentTimeMillis() / 1000;
        long age = Math.abs(currentTime - receivedTimestamp);
        
        boolean isValid = age <= maxAgeSeconds;
        if (!isValid) {
            log.warn("Timestamp validation failed. Current time: {}, Received timestamp: {}, Age: {} seconds (max: {})",
                    currentTime, receivedTimestamp, age, maxAgeSeconds);
        }
        return isValid;
    }

    /**
     * Calculates HMAC-SHA256 hash of the payload.
     *
     * @param payload the payload to hash
     * @param secret the secret key
     * @return the HMAC hash in hexadecimal format
     * @throws Exception if calculation fails
     */
    public static String calculateHmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                0,
                secret.getBytes(StandardCharsets.UTF_8).length,
                ALGORITHM
        );
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     *
     * @param a first string
     * @param b second string
     * @return true if strings are equal, false otherwise
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        
        int result = 0;
        result |= aBytes.length ^ bBytes.length;
        
        for (int i = 0; i < Math.min(aBytes.length, bBytes.length); i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        
        return result == 0;
    }

    /**
     * Converts byte array to hexadecimal string.
     *
     * @param bytes the bytes to convert
     * @return hexadecimal representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
