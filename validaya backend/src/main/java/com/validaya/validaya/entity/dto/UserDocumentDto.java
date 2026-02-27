package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.DocumentSource;
import com.validaya.validaya.entity.enums.DocumentStatus;
import com.validaya.validaya.entity.enums.VerificationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class UserDocumentDto {

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private Long documentTypeId;
        private String documentTypeName;
        private String documentNumber;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private DocumentStatus status;
        private VerificationStatus verificationStatus;
        private DocumentSource source;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CreateRequest {
        private Long documentTypeId;
        private String documentNumber;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private Map<String, Object> dataPayload;
    }
}