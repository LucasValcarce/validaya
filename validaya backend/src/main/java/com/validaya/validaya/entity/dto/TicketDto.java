package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

public class TicketDto {

    @Data
    public static class Response {
        private Long id;
        private String code;
        private Long applicationId;
        private String applicationNumber;
        private TicketStatus status;
        private String qrPayload;
        private LocalDateTime expiryAt;
        private LocalDateTime usedAt;
        private LocalDateTime createdAt;
    }
}