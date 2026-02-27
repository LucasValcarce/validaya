package com.validaya.validaya.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class CryptoRSA {

    @Value("${validaya.public-key:}")
    private String publicKey;

    public CryptoRSA() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String rsaEncryptionOaepSha256(String plaintext) throws GeneralSecurityException {
        return base64Encoding(rsaEncryptionOaepSha256(getPublicKeyFromString(publicKey),
                plaintext.getBytes(StandardCharsets.UTF_8), CipherSuite.RSA));
    }

    public byte[] rsaEncryptionOaepSha256(PublicKey publicKey, byte[] plaintextByte, CipherSuite suite)
            throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchPaddingException {
        Cipher encryptCipher = Cipher.getInstance(suite.getAlgorithm());
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encryptCipher.doFinal(plaintextByte);
    }

    public static String base64Encoding(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    public PublicKey getPublicKeyFromString(String key) throws GeneralSecurityException {
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("[\\r\\n]+", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(encoded));
    }
}