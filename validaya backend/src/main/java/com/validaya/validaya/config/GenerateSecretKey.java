package com.validaya.validaya.config;

import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerateSecretKey {
    public static void main(String[] args) {
        SecretKey secretKey = io.jsonwebtoken.security.Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secretString = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Clave secreta segura (Base64): " + secretString);
    }
}