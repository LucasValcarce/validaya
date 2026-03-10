package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response DTO for cancel transaction endpoint in Stereum Pay API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SterumCancelResponse {

    /**
     * Transaction ID
     */
    private String id;

    /**
     * New transaction status after cancellation
     */
    private String status;

    /**
     * Success message
     */
    private String message;

    /**
     * Timestamp of cancellation
     */
    @JsonProperty("cancelled_at")
    private Long cancelledAt;
}
