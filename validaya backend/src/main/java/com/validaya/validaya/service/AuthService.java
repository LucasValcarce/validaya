package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AuthDto;

public interface AuthService {

    AuthDto.AuthResponse login(AuthDto.LoginRequest request);

    AuthDto.AuthResponse register(AuthDto.RegisterRequest request);

    void logout(String token);
}