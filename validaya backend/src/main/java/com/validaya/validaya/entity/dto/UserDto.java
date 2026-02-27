package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class Response {
        private Long id;
        private String email;
        private String fullName;
        private String identification;
        private String phone;
        private LocalDate birthDate;
        private UserType userType;
        private Boolean faceVerified;
        private LocalDateTime faceRegisteredAt;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateRequest {
        private String fullName;
        private String phone;
        private LocalDate birthDate;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
    }
}