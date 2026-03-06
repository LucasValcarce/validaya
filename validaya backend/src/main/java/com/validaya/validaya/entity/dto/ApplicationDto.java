package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDto {

    @Data
    public static class CreateRequest {
        @NotNull
        private Long procedureId;
        @NotNull
        private Long institutionId;
        private Long branchId;
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private String applicationNumber;
        private Long userId;
        private String userName;
        private Long procedureId;
        private String procedureName;
        private Long institutionId;
        private String institutionName;
        private Long branchId;
        private String branchName;
        private ApplicationStatus status;
        private BigDecimal totalAmount;
        private String notes;
        private String rejectionReason;
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private List<ApplicationDocumentDto.Response> documents;
        
        // Información de validación automática
        private boolean documentValidationCompleted;
        private boolean allDocumentsPresent;
        private List<DocumentValidationResponse.RequiredDocumentDto> missingDocuments;
    }

    @Data
    public static class DocumentValidationResponse {
        private Long applicationId;
        private String applicationNumber;
        private ApplicationStatus status;
        private boolean allDocumentsPresent;
        private List<RequiredDocumentDto> requiredDocuments;
        private List<RequiredDocumentDto> missingDocuments;
        
        @Data
        public static class RequiredDocumentDto {
            private Long documentTypeId;
            private String documentTypeName;
            private boolean isMandatory;
            private Integer maxAgeMonths;
            private String notes;
            private boolean isPresent;
            private LocalDateTime userDocumentCreatedAt;
        }
    }

    @Data
    public static class ReviewRequest {
        @NotNull
        private ApplicationStatus newStatus;
        private String rejectionReason;
    }

    @Data
    public static class Summary {
        private Long id;
        private String applicationNumber;
        private String procedureName;
        private String institutionName;
        private ApplicationStatus status;
        private LocalDateTime createdAt;
    }
}