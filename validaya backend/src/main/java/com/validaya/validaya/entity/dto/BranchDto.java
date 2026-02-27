package com.validaya.validaya.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

public class BranchDto {

    @Data
    public static class Response {
        private Long id;
        private Long institutionId;
        private String institutionName;
        private String name;
        private String address;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String phone;
        private Map<String, Object> schedule;
        private Integer maxDailyAppointments;
        private Boolean isActive;
    }

    @Data
    public static class CreateRequest {
        @NotNull
        private Long institutionId;
        @NotBlank
        private String name;
        private String address;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String phone;
        private Map<String, Object> schedule;
        private Integer maxDailyAppointments;
    }
}