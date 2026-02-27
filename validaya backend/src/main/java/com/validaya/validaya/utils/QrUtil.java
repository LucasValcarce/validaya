package com.validaya.validaya.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Genera el payload firmado para los códigos QR de tickets.
 * En producción, firmar con HMAC-SHA256 usando una clave secreta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrUtil {

    @Autowired(required = false)
    private final ObjectMapper objectMapper;

    public String generatePayload(String ticketCode, Long applicationId, String applicationNumber) {
        try {
            Map<String, Object> payload = Map.of(
                    "code", ticketCode,
                    "appId", applicationId,
                    "appNum", applicationNumber,
                    "ts", LocalDateTime.now().toString(),
                    "v", 1
            );
            String json = objectMapper.writeValueAsString(payload);
            // Base64 simple — en producción firmar con HMAC-SHA256
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error generando QR payload para ticket: {}", ticketCode, e);
            throw new RuntimeException("Error generando QR", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decodePayload(String qrPayload) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(qrPayload);
            return objectMapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            log.error("Error decodificando QR payload", e);
            throw new RuntimeException("QR inválido", e);
        }
    }
}