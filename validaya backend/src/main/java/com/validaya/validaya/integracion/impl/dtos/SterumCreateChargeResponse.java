package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response DTO for create charge endpoint in Stereum Pay API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SterumCreateChargeResponse {

    /**
     * Transaction ID from Stereum
     */
    private String id;

    /**
     * Amount charged
     */
    private Double amount;

    /**
     * Currency used
     */
    private String currency;

    /**
     * Blockchain network
     */
    private String network;

    /**
     * QR code in Base64 format
     */
    @JsonProperty("qr_base64")
    private String qrBase64;

    /**
     * Payment link for users
     */
    @JsonProperty("payment_link")
    private String paymentLink;

    /**
     * Transaction status
     */
    @JsonProperty("transaction_status")
    private String transactionStatus;

    /**
     * Main network flag
     */
    @JsonProperty("on_main_net")
    private Boolean onMainNet;

    /**
     * Collecting account address
     */
    @JsonProperty("collecting_account")
    private String collectingAccount;

    /**
     * Expiration time (unix timestamp)
     */
    @JsonProperty("expiration_time")
    private Long expirationTime;

    /**
     * Merchant reference
     */
    private String reference;

    /**
     * Webhook notification URL
     */
    private String callback;

    /**
     * Creation timestamp
     */
    @JsonProperty("created_at")
    private Long createdAt;
}
