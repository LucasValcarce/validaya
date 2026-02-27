package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.PaymentMethod;
import com.validaya.validaya.entity.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data
    public static class InitiateRequest {
        @NotNull
        private Long applicationId;
        @NotNull
        private PaymentMethod paymentMethod;
    }

    @Data
    public static class Response {
        private Long id;
        private Long applicationId;
        private String applicationNumber;
        private String transactionId;
        private BigDecimal amount;
        private BigDecimal platformFee;
        private BigDecimal institutionAmount;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private String gatewayUrl;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
    }

    @Data
    public static class GatewayCallback {
        private String transactionId;
        private String externalReference;
        private String status;
        private String gatewayResponse;
    }
}