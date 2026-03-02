package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.AuthDto;
import com.validaya.validaya.service.BiometricsService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class BiometricsController {

    private final BiometricsService biometricsService;

    @PostMapping("/enroll/init")
    public ResponseEntity<ApiResponse<String>> enrollInit(@RequestBody EnrollInitRequest req) {
        String session = biometricsService.initEnrollment(req.getIdentification(), req.getFullName());
        return ResponseEntity.ok(ApiResponse.ok("Enrollment session created", session));
    }

    @PostMapping("/enroll/face")
    public ResponseEntity<ApiResponse<Boolean>> enrollFace(
            @RequestHeader("X-Enrollment-Session") String session,
            @RequestBody FaceUploadRequest req) {
        boolean ok = biometricsService.submitFace(session, req.getFaceBase64());
        if (!ok) return ResponseEntity.badRequest().body(ApiResponse.error("Invalid session or user not found"));
        return ResponseEntity.ok(ApiResponse.ok("Face submitted", true));
    }

    @PostMapping("/login/face")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> loginByFace(@RequestBody FaceUploadRequest req) {
        AuthDto.AuthResponse resp = biometricsService.authenticateByFace(req.getFaceBase64());
        if (resp == null) return ResponseEntity.status(401).body(ApiResponse.error("Face not recognized"));
        return ResponseEntity.ok(ApiResponse.ok("Authenticated", resp));
    }

    @Data
    public static class EnrollInitRequest {
        @NotBlank
        private String identification;
        private String fullName;
    }

    @Data
    public static class FaceUploadRequest {
        @NotBlank
        private String faceBase64;
    }
}
