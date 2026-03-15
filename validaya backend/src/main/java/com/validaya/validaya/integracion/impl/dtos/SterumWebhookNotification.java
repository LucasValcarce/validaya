package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Webhook notification DTO from Stereum Pay
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SterumWebhookNotification {

    /**
     * Notification unique identifier
     */
    private String id;

    /**
     * Type of notification: "transaction" or "test"
     */
    @JsonProperty("notification_type")
    private String notificationType;

    /**
     * Transaction details
     */
    private SterumTransaction transaction;

    /**
     * Merchant reference
     */
    private String reference;

    /**
     * Stereum notification timestamp in milliseconds
     */
    private Long timestamp;

    /**
     * Creation timestamp (alternative field name)
     */
    @JsonProperty("created_at")
    private Long createdAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SterumTransaction {
        /**
         * Transaction ID
         */
        private String id;

        /**
         * Amount
         */
        private Double amount;

        /**
         * Currency
         */
        private String currency;

        /**
         * Status: INICIADO, PENDIENTE, PAGADO, CANCELADO, ERROR
         */
        private String status;

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
         * Payment date
         */
        @JsonProperty("paid_at")
        private Long paidAt;

        /**
         * Error message if any
         */
        private String error;
    }
}
