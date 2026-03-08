package com.validaya.validaya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.validaya.validaya.config.SterumPayProperties;
import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.integracion.impl.dtos.SterumWebhookNotification;
import com.validaya.validaya.service.PaymentService;
import com.validaya.validaya.service.impl.PaymentServiceImpl;
import com.validaya.validaya.utils.HmacValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook controller for Stereum Pay notifications.
 * Handles payment status updates and transaction notifications.
 * 
 * Security measures:
 * - HMAC-SHA256 signature validation
 * - Timestamp freshness validation (prevents replay attacks)
 * - Request body size limits
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private final SterumPayProperties sterumPayProperties;
    private final ObjectMapper objectMapper;

    /**
     * Receives and processes webhook notifications from Stereum Pay.
     * 
     * Validates:
     * 1. HMAC-SHA256 signature using webhook secret
     * 2. Timestamp freshness (default 5 minutes)
     * 3. Notification type
     * 
     * Handles:
     * - "test" notifications (responds immediately without processing)
     * - "transaction" notifications (processes payment status updates)
     *
     * @param signature x-signature header with HMAC-SHA256 signature
     * @param xTimestamp x-timestamp header with unix timestamp (seconds)
     * @param body raw JSON request body
     * @return webhook response
     */
    @PostMapping(
            value = "/stereum",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> handleSterumWebhook(
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-timestamp") Long xTimestamp,
            @RequestBody String body) {

        log.info("Received Stereum webhook notification");
        log.debug("xTimestamp: {}", xTimestamp);

        try {
            // Step 1: Validate timestamp (prevent replay attacks)
            if (!HmacValidationUtil.validateTimestamp(xTimestamp, sterumPayProperties.getWebhookMaxAge())) {
                log.warn("Webhook timestamp validation failed. Max age: {} seconds", 
                        sterumPayProperties.getWebhookMaxAge());
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Webhook timestamp expired")
                );
            }

            // Step 2: Validate HMAC signature
            if (!HmacValidationUtil.validateSignature(
                    body,
                    signature,
                    sterumPayProperties.getWebhookSecret())) {
                log.error("Webhook signature validation failed");
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Webhook signature invalid")
                );
            }

            log.debug("Webhook signature and timestamp validated successfully");

            // Step 3: Parse notification
            SterumWebhookNotification notification;
            try {
                notification = objectMapper.readValue(body, SterumWebhookNotification.class);
            } catch (Exception e) {
                log.error("Error parsing webhook notification: {}", e.getMessage(), e);
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Invalid webhook payload")
                );
            }

            log.info("Parsed webhook notification. Type: {}, Transaction ID: {}", 
                    notification.getNotificationType(),
                    notification.getTransaction() != null ? notification.getTransaction().getId() : "N/A");

            // Step 4: Handle test notifications
            if ("test".equalsIgnoreCase(notification.getNotificationType())) {
                log.info("Received test notification from Stereum");
                return ResponseEntity.ok(ApiResponse.ok("Test notification received"));
            }

            // Step 5: Handle transaction notifications
            if (!"transaction".equalsIgnoreCase(notification.getNotificationType())) {
                log.warn("Unknown notification type: {}", notification.getNotificationType());
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Unknown notification type")
                );
            }

            if (notification.getTransaction() == null || notification.getTransaction().getId() == null) {
                log.error("Transaction data missing in webhook notification");
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Transaction data missing")
                );
            }

            // Step 6: Process transaction notification
            String transactionId = notification.getTransaction().getId();
            String status = notification.getTransaction().getStatus();

            log.info("Processing transaction notification. ID: {}, Status: {}", transactionId, status);

            // Create callback from webhook data
            PaymentDto.GatewayCallback callback = new PaymentDto.GatewayCallback();
            callback.setTransactionId(transactionId);
            callback.setStatus(status);
            callback.setGatewayResponse(body);

            // Process payment update
            try {
                paymentService.handleGatewayCallback(callback);
                log.info("Successfully processed transaction {} with status {}", transactionId, status);
            } catch (Exception e) {
                log.error("Error processing transaction callback: {}", e.getMessage(), e);
                return ResponseEntity.internalServerError().body(
                        ApiResponse.error("Error processing transaction")
                );
            }

            return ResponseEntity.ok(ApiResponse.ok("Webhook processed successfully"));

        } catch (Exception e) {
            log.error("Unexpected error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Webhook processing failed")
            );
        }
    }

    /**
     * Health check endpoint for webhook configuration.
     * Returns webhook configuration status without sensitive data.
     *
     * @return webhook status
     */
    @GetMapping("/stereum/status")
    public ResponseEntity<?> getWebhookStatus() {
        log.info("Webhook status check requested");
        
        WebhookStatus status = WebhookStatus.builder()
                .active(true)
                .maxAge(sterumPayProperties.getWebhookMaxAge())
                .url("/api/v1/webhooks/stereum")
                .build();
        
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    /**
     * DTO for webhook status response
     */
    @lombok.Data
    @lombok.Builder
    public static class WebhookStatus {
        private boolean active;
        private long maxAge;
        private String url;
    }
}
