package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AuthDto;

public interface AuthService {

    /**
     * Paso 1: Verificar que el usuario existe por su carnet de identidad.
     */
    AuthDto.IdentifyResponse identify(String identification);

    /**
     * Paso 2: Verificar el rostro del usuario.
     * Si el rostro coincide, retorna un JWT temporal para establecer contraseña.
     */
    AuthDto.FaceVerificationResponse verifyFace(String identification, String faceBase64);

    /**
     * Paso 3: Establecer contraseña (requiere JWT válido).
     */
    AuthDto.AuthResponse setPassword(Long userId, String password);

    /**
     * Login tradicional con identificación y contraseña (para después que el usuario establezca contraseña).
     */
    AuthDto.AuthResponse login(AuthDto.LoginRequest request);

    void logout(String token);
}