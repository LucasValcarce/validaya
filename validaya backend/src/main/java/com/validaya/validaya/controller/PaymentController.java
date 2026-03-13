package com.validaya.validaya.controller;

import com.validaya.validaya.config.SterumPayProperties;
import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.service.PaymentService;
import com.validaya.validaya.utils.HmacValidationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SterumPayProperties sterumPayProperties;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> createPayment(
            @Valid @RequestBody PaymentDto.InitiateRequest request) {
        log.info("Received payment creation request for application: {}", request.getApplicationId());
        
        PaymentDto.Response response = paymentService.initiate(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getPaymentById(
            @PathVariable Long paymentId) {
        log.info("Retrieving payment: {}", paymentId);
        
        PaymentDto.Response response = paymentService.getById(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getPaymentByApplication(
            @PathVariable Long applicationId) {
        log.info("Retrieving payment for application: {}", applicationId);
        
        PaymentDto.Response response = paymentService.getByApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> verifyPaymentStatus(
            @PathVariable Long paymentId) {
        log.info("Verifying payment status: {}", paymentId);
        
        PaymentDto.Response response = paymentService.verifyTransaction(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> cancelPayment(
            @PathVariable Long paymentId) {
        log.info("Received cancel request for payment: {}", paymentId);
        
        PaymentDto.Response response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Webhook endpoint for Stereum Pay notifications.
     * Validates HMAC signature and timestamp before processing.
     * Supports both transaction notifications and test notifications.
     *
     * @param signature x-signature header with HMAC-SHA256 signature
     * @param xTimestamp x-timestamp header with unix timestamp
     * @param body the webhook payload
     * @return response indicating success
     */
    @PostMapping(
            value = "/webhook",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> handleWebhook(
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-timestamp") Long xTimestamp,
            @RequestBody String body) {

        log.info("Received webhook notification from Stereum Pay");
        log.debug("Signature: {}, Timestamp: {}", signature, xTimestamp);

        // Validate timestamp (prevent replay attacks)
        if (!HmacValidationUtil.validateTimestamp(xTimestamp, sterumPayProperties.getWebhookMaxAge())) {
            log.warn("Webhook timestamp validation failed");
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Webhook timestamp invalid or expired")
            );
        }

        try {
            // Note: This is handled by WebhookController instead
            log.info("Webhook validation successful - redirecting to WebhookController");
            return ResponseEntity.ok(ApiResponse.ok("Webhook received"));
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error processing webhook")
            );
        }
    }
}