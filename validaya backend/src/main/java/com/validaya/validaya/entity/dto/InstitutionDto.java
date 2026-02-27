package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InstitutionDto {

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String slug;
        private InstitutionType type;
        private String description;
        private String logoUrl;
        private String website;
        private String contactEmail;
        private String contactPhone;
        private Boolean isActive;
        private List<BranchDto.Response> branches;
    }

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String slug;
        private InstitutionType type;
        private String description;
        private String website;
        private String contactEmail;
        private String contactPhone;
    }

    @Data
    public static class Summary {
        private Long id;
        private String name;
        private String slug;
        private InstitutionType type;
        private String logoUrl;
    }
}