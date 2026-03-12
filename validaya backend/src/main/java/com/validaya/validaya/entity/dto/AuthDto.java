package com.validaya.validaya.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class IdentifyRequest {
        @NotBlank
        private String identification;
        @NotBlank
        private String faceBase64;
    }

    @Data
    public static class IdentifyResponse {
        private boolean exists;        // true si el usuario existe
        private boolean verified;      // true si el rostro coincide
        private String token;          // JWT temporal (válido para establecer contraseña)
        private Long userId;
        private String email;
        private String fullName;
        private String userType;
        private double confidence;     // Confianza de la verificación facial
        private String message;        // Mensaje descriptivo del resultado
    }

    @Data
    public static class SetPasswordRequest {
        @NotBlank @Size(min = 8)
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private Long userId;
        private String email;
        private String fullName;
        private String userType;
        private boolean passwordSet;
    }

    /**
     * Login mejorado: CI + contraseña + fotografía del rostro
     * Verifica primero contraseña en BD, luego verifica rostro con modelado facial
     */
    @Data
    public static class LoginRequest {
        @NotBlank
        private String identification;  // Carnet de identidad (CI)
        @NotBlank
        private String password;        // Contraseña
        @NotBlank
        private String faceBase64;      // Imagen del rostro en Base64
    }
}