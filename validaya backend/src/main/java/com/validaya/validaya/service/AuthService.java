package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AuthDto;

public interface AuthService {

    AuthDto.IdentifyResponse identify(String identification, String faceBase64);

    AuthDto.AuthResponse setPassword(Long userId, String password);

    AuthDto.AuthResponse login(AuthDto.LoginRequest request);

    void logout(String token);
}