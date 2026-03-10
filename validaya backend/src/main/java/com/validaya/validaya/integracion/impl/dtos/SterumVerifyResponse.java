package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response DTO for verify transaction endpoint in Stereum Pay API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SterumVerifyResponse {

    /**
     * Transaction ID
     */
    private String id;

    /**
     * Transaction status (INICIADO, PENDIENTE, PAGADO, CANCELADO, ERROR)
     */
    private String status;

    /**
     * Amount
     */
    private Double amount;

    /**
     * Currency
     */
    private String currency;

    /**
     * Network
     */
    private String network;

    /**
     * Paid amount
     */
    @JsonProperty("paid_amount")
    private Double paidAmount;

    /**
     * Paid currency
     */
    @JsonProperty("paid_currency")
    private String paidCurrency;

    /**
     * Blockchain transaction hash
     */
    @JsonProperty("tx_hash")
    private String txHash;

    /**
     * Confirmation count
     */
    private Integer confirmations;

    /**
     * Payment timestamp
     */
    @JsonProperty("paid_at")
    private Long paidAt;

    /**
     * Error message if any
     */
    private String error;

    /**
     * Merchant reference
     */
    private String reference;
}
