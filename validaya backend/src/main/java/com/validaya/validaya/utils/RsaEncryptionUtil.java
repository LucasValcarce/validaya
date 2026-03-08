package com.validaya.validaya.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for RSA encryption operations.
 * Used for encrypting passwords before sending to Stereum Pay API.
 */
@Slf4j
public class RsaEncryptionUtil {

    private static final String ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * Encrypts a plaintext string using RSA public key.
     *
     * @param plaintext the text to encrypt
     * @param publicKeyPem the public key in PEM format
     * @return the encrypted text in Base64 format
     * @throws RuntimeException if encryption fails
     */
    public static String encrypt(String plaintext, String publicKeyPem) {
        try {
            PublicKey publicKey = loadPublicKey(publicKeyPem);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Error during RSA encryption: {}", e.getMessage(), e);
            throw new RuntimeException("RSA encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a public key from PEM format.
     *
     * @param publicKeyPem the public key in PEM format (with -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY-----)
     * @return the PublicKey object
     * @throws Exception if key loading fails
     */
    private static PublicKey loadPublicKey(String publicKeyPem) throws Exception {
        // Remove PEM header and footer
        String publicKeyContent = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\n", "")
                .trim();

        // Decode from Base64
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyContent);

        // Create PublicKey from bytes
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(spec);
    }
}
