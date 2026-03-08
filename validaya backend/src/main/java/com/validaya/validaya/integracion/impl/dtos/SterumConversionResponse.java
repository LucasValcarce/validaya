package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Response DTO for currency conversion endpoint in Stereum Pay API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SterumConversionResponse {

    /**
     * Country code
     */
    private String country;

    /**
     * Source currency
     */
    @JsonProperty("from_currency")
    private String fromCurrency;

    /**
     * Target currency
     */
    @JsonProperty("to_currency")
    private String toCurrency;

    /**
     * Original amount
     */
    private Double amount;

    /**
     * Converted amount
     */
    @JsonProperty("converted_amount")
    private Double convertedAmount;

    /**
     * Exchange rate
     */
    @JsonProperty("exchange_rate")
    private Double exchangeRate;
}
