package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AuthDto;

public interface BiometricsService {

    String initEnrollment(String identification, String fullName);

    boolean submitFace(String sessionToken, String faceBase64);

    AuthDto.AuthResponse authenticateByFace(String faceBase64);
}
