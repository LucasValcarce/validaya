package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.AppDocVerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class ApplicationDocumentDto {

    @Data
    public static class Response {
        private Long id;
        private Long applicationId;
        private Long userDocumentId;
        private String documentTypeName;
        private Long requirementId;
        private AppDocVerificationStatus verificationStatus;
        private String rejectionReason;
        private Long reviewedBy;
        private LocalDateTime reviewedAt;
    }

    @Data
    public static class SubmitRequest {
        @NotNull
        private Long applicationId;
        @NotNull
        private Long requirementId;
        @NotNull
        private Long userDocumentId;
    }

    @Data
    public static class ReviewRequest {
        @NotNull
        private AppDocVerificationStatus verificationStatus;
        private String rejectionReason;
    }
}