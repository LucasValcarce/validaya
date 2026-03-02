package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AuthDto;

public interface BiometricsService {

    /**
     * Initialize an enrollment session for a user identified by `identification`.
     * Returns a session token to be used when uploading face data.
     */
    String initEnrollment(String identification, String fullName);

    /**
     * Submit face payload for an enrollment session (base64).
     */
    boolean submitFace(String sessionToken, String faceBase64);

    /**
     * Authenticate a user by face payload. Returns AuthResponse (token, user info) or null.
     */
    AuthDto.AuthResponse authenticateByFace(String faceBase64);
}
