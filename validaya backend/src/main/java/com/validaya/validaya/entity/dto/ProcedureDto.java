package com.validaya.validaya.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class ProcedureDto {

    @Data
    public static class Response {
        private Long id;
        private Long institutionId;
        private String institutionName;
        private String name;
        private String slug;
        private String description;
        private Integer estimatedDays;
        private BigDecimal basePrice;
        private BigDecimal platformFee;
        private BigDecimal totalPrice;
        private Boolean isActive;
        private Long outputDocumentTypeId;
        private String outputDocumentTypeName;
        private List<RequirementResponse> requirements;
    }

    @Data
    public static class RequirementResponse {
        private Long id;
        private Long documentTypeId;
        private String documentTypeName;
        private Boolean isMandatory;
        private Integer maxAgeMonths;
    }

    @Data
    public static class CreateRequest {
        @NotNull
        private Long institutionId;
        @NotBlank
        private String name;
        private String description;
        private Integer estimatedDays;
        private BigDecimal basePrice;
        private BigDecimal platformFee;
        private Long outputDocumentTypeId;
    }

    @Data
    public static class Summary {
        private Long id;
        private String name;
        private String slug;
        private String institutionName;
        private BigDecimal totalPrice;
        private Integer estimatedDays;
    }
}