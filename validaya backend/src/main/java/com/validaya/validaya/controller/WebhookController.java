package com.validaya.validaya.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.validaya.validaya.config.SterumPayProperties;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.exception.OperationException;
import com.validaya.validaya.integracion.impl.dtos.SterumWebhookNotification;
import com.validaya.validaya.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private final SterumPayProperties sterumPayProperties;

    @PostMapping(
            value = "/stereum",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<Void> handleSterumWebhook(
            @RequestHeader("x-signature") String signature,
            @RequestHeader("x-timestamp") long xTimestamp,
            @RequestBody String body) {

        log.info("Received inbound request with headers - x-signature: {}, x-timestamp: {}", signature, xTimestamp);
        log.info("Request body received: {}", body);

        String apiKey = sterumPayProperties.getApiKey();
        String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256,
                apiKey.getBytes(StandardCharsets.UTF_8))
                .hmacHex(body.getBytes(StandardCharsets.UTF_8));

        log.info("Calculated HMAC: {}", hmac);

        if (!signature.equals(hmac)) {
            log.error("Signature validation failed. Expected: {}, Actual: {}", hmac, signature);
            throw new OperationException("Error en la firma");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SterumWebhookNotification notification;
        try {
            notification = objectMapper.readValue(body, SterumWebhookNotification.class);
            log.info("Successfully parsed request body to SterumWebhookNotification: {}", notification);
        } catch (Exception e) {
            log.error("Failed to parse request body. Error: {}", e.getMessage(), e);
            throw new OperationException("Error en la firma");
        }

        if ("test".equalsIgnoreCase(notification.getNotificationType())) {
            log.info("Received test notification");
            return ok().build();
        }

        if (!"transaction".equalsIgnoreCase(notification.getNotificationType())) {
            log.warn("Invalid notification type received: {}", notification.getNotificationType());
            throw new OperationException("No corresponde a este método la notificación");
        }

        try {
            if (notification.getTransaction() == null || notification.getTransaction().getId() == null) {
                log.error("Transaction ID is null in the received notification");
                throw new OperationException("No se encontró el id de la transacción");
            }

            log.info("Processing transaction notification for ID: {}", notification.getTransaction().getId());

            // Create callback from webhook data
            PaymentDto.GatewayCallback callback = new PaymentDto.GatewayCallback();
            callback.setTransactionId(notification.getTransaction().getId());
            callback.setStatus(notification.getTransaction().getStatus());
            callback.setGatewayResponse(body);

            // Process payment update
            paymentService.handleGatewayCallback(callback);
            log.info("Successfully processed transaction {} with status {}", 
                    notification.getTransaction().getId(), 
                    notification.getTransaction().getStatus());

            return ok().build();
        } catch (OperationException e) {
            log.error("OperationException while processing Stereum notification for transaction {}. Cause: {}",
                    notification != null && notification.getTransaction() != null ? notification.getTransaction().getId() : "null", 
                    e.getMessage(), e);
            throw new OperationException("Se generó un error al recibir notificación de Stereum");
        } catch (Exception e) {
            log.error("Unexpected error while processing Stereum confirmation for transaction {}",
                    notification != null && notification.getTransaction() != null ? notification.getTransaction().getId() : "null", e);
            throw new OperationException("Se generó un error al recibir la confirmación de Stereum");
        }
    }
}
