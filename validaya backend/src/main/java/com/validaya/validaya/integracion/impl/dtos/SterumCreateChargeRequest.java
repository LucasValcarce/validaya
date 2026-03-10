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
    private String country;
    private String amount;
    private String network;
    private String currency;
    @JsonProperty("idempotency_key")
    private String idempotencyKey;
    @JsonProperty("charge_reason")
    private String chargeReason;
    private String callback;
    private SterumCustomerData customer;
    private String reference;
}
