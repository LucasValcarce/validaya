package com.validaya.validaya.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    /**
     * Paso 1: Verificar que el usuario existe por su carnet de identidad
     */
    @Data
    public static class IdentifyRequest {
        @NotBlank
        private String identification; // Carnet de identidad
    }

    @Data
    public static class IdentifyResponse {
        private boolean exists; // true si el usuario existe
        private Long userId;
        private String fullName;
        private String message; // "Usuario encontrado" o "Usuario no encontrado"
    }

    /**
     * Paso 2: Verificar rostro (face recognition)
     */
    @Data
    public static class VerifyFaceRequest {
        @NotBlank
        private String identification;
        @NotBlank
        private String faceBase64; // Vector facial en base64
    }

    @Data
    public static class FaceVerificationResponse {
        private boolean verified; // true si el rostro coincide
        private String token; // JWT temporal (válido para establecer contraseña)
        private Long userId;
        private String email;
        private String fullName;
        private String userType;
        private String message;
    }

    /**
     * Paso 3: Establecer contraseña (solo después de verificar cara)
     */
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
     * Login tradicional (legacy, puede usarse con contraseña después de ser establecida)
     */
    @Data
    public static class LoginRequest {
        @NotBlank
        private String identification;
        @NotBlank
        private String password;
    }
}