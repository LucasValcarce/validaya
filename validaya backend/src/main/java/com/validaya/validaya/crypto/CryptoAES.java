package com.validaya.validaya.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado AES-256-GCM para campos sensibles (face_vector, credentials, data_payload).
 * El resultado incluye el IV prepended al ciphertext en Base64.
 */
@Slf4j
@Component
public class CryptoAES {

    @Value("${validaya.aes-secret-key}")
    private String aesSecretKey;

    private static final CipherSuite SUITE = CipherSuite.AESGCMNoPadding256;

    /**
     * Cifra un texto plano con AES-256-GCM.
     * Formato de salida: Base64(IV[12 bytes] + CipherText)
     */
    public String encrypt(String plaintext) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(aesSecretKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[SUITE.getIvSize()];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(SUITE.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(SUITE.getTagLength(), iv));

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Descifra un texto cifrado con AES-256-GCM.
     * Espera formato: Base64(IV[12 bytes] + CipherText)
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(aesSecretKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[SUITE.getIvSize()];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(SUITE.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(SUITE.getTagLength(), iv));

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Genera una nueva clave AES-256 aleatoria en Base64 (para setup inicial).
     */
    public static String generateKey() {
        byte[] key = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}