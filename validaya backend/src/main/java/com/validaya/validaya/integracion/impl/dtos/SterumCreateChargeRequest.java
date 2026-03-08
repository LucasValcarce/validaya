package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request DTO for creating a charge in Stereum Pay API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SterumCreateChargeRequest {

    /**
     * Country code (e.g., BO for Bolivia)
     */
    private String country;

    /**
     * Amount to charge
     */
    private String amount;

    /**
     * Blockchain network (e.g., POLYGON)
     */
    private String network;

    /**
     * Blockchain currency (e.g., USDT)
     */
    private String currency;

    /**
     * Idempotency key for preventing duplicate charges
     */
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    /**
     * Reason for the charge
     */
    @JsonProperty("charge_reason")
    private String chargeReason;

    /**
     * Callback URL for notifications
     */
    private String callback;

    /**
     * Customer information
     */
    private SterumCustomerData customer;

    /**
     * Custom reference for merchant
     */
    private String reference;
}
