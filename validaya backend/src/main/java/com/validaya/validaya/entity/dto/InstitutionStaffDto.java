package com.validaya.validaya.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class InstitutionStaffDto {

    @Data
    public static class AssignRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Long institutionId;
        @NotNull
        private boolean isAdmin; // true = institution_admin, false = staff
        private Long branchId;
    }

    @Data
    public static class UpdateRequest {
        @NotNull
        private boolean isAdmin; // true = institution_admin, false = staff
        private Long branchId;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private String userEmail;
        private String userFullName;
        private String userIdentification;
        private String userType; // citizen, staff, institution_admin, admin
        private Long institutionId;
        private String institutionName;
        private String employeeCode;
        private Long branchId;
        private String branchName;
        private Boolean isActive;
        private LocalDateTime assignedAt;
    }
}
