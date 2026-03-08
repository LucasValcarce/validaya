package com.validaya.validaya.integracion.impl;

import com.validaya.validaya.config.SterumPayProperties;
import com.validaya.validaya.integracion.SterumPayService;
import com.validaya.validaya.integracion.impl.dtos.*;
import com.validaya.validaya.utils.RsaEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of Stereum Pay API service.
 * Handles authentication, JWT token management, API calls with retry logic,
 * RSA encryption for passwords, and proper error handling.
 */
@Slf4j
@Service
public class SterumPayServiceImpl implements SterumPayService {

    private final SterumPayProperties properties;
    private final RestClient restClient;

    // Token management
    private volatile String jwtToken = null;
    private volatile long tokenExpirationTime = 0;

    public SterumPayServiceImpl(SterumPayProperties properties) {
        this.properties = properties;
        this.restClient = createRestClient();
    }

    /**
     * Obtains JWT token from Stereum Pay API with automatic renewal.
     * Encrypts password using RSA before sending.
     */
    @Override
    public StereumAuthResponse obtienTokenAutenticacion() {
        log.info("Initiating authentication with Stereum Pay");
        return authenticateWithRetry();
    }

    /**
     * Authenticates with Stereum Pay API with retry logic.
     */
    private StereumAuthResponse authenticateWithRetry() {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxRetries()) {
            try {
                // Encrypt password using RSA
                String encryptedPassword = RsaEncryptionUtil.encrypt(
                        properties.getPassword(),
                        properties.getPublicKey()
                );

                StereumAuthRequest request = new StereumAuthRequest();
                request.setUsername(properties.getUsername());
                request.setPassword(encryptedPassword);

                log.debug("Sending authentication request to Stereum Pay");

                ResponseEntity<StereumAuthResponse> response = restClient.post()
                        .uri(properties.getBaseUrl() + "/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(StereumAuthResponse.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Stereum authentication failed with status: {}", response.getStatusCode());
                    throw new RuntimeException("Authentication failed with status: " + response.getStatusCode());
                }

                StereumAuthResponse authResponse = response.getBody();
                
                // Update JWT token and expiration time
                this.jwtToken = authResponse.getAccessToken();
                
                // Set expiration time: current time + expires_in (typically 8 hours = 28800 seconds)
                long expiresIn = authResponse.getExpires_in() != null 
                        ? Long.parseLong(authResponse.getExpires_in()) 
                        : 28800; // Default to 8 hours
                this.tokenExpirationTime = Instant.now().getEpochSecond() + expiresIn;

                log.info("Authentication successful. Token will expire at: {}", 
                        Instant.ofEpochSecond(tokenExpirationTime));
                return authResponse;

            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Authentication attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                
                if (attempt < properties.getMaxRetries()) {
                    try {
                        Thread.sleep(properties.getRetryDelay() * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry delay", ie);
                    }
                }
            }
        }

        log.error("Authentication failed after {} attempts", properties.getMaxRetries());
        throw new RuntimeException("Failed to authenticate with Stereum Pay after " + 
                properties.getMaxRetries() + " attempts", lastException);
    }

    /**
     * Creates a charge in Stereum Pay with retry logic.
     */
    @Override
    public SterumCreateChargeResponse crearCargoCobro(SterumCreateChargeRequest request, Long applicationId) {
        log.info("Creating charge for application: {}", applicationId);
        
        // Ensure token is valid before making the call
        ensureTokenIsValid();

        // Set idempotency key if not set
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            request.setIdempotencyKey(generateIdempotencyKey());
        }

        return createChargeWithRetry(request, applicationId);
    }

    /**
     * Creates a charge with retry logic.
     */
    private SterumCreateChargeResponse createChargeWithRetry(SterumCreateChargeRequest request, Long applicationId) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxRetries()) {
            try {
                log.debug("Sending create charge request (attempt {})", attempt + 1);

                ResponseEntity<SterumCreateChargeResponse> response = restClient.post()
                        .uri(properties.getBaseUrl() + "/api/v1/transactions/create-charge")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(SterumCreateChargeResponse.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Create charge failed with status: {}", response.getStatusCode());
                    throw new RuntimeException("Create charge failed: " + response.getStatusCode());
                }

                SterumCreateChargeResponse chargeResponse = response.getBody();
                log.info("Charge created successfully. Transaction ID: {}, Application ID: {}", 
                        chargeResponse.getId(), applicationId);
                return chargeResponse;

            } catch (HttpClientErrorException.Unauthorized e) {
                log.warn("Token unauthorized (401), refreshing token and retrying");
                refrescarToken();
                attempt++;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Create charge attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                
                if (attempt < properties.getMaxRetries()) {
                    try {
                        Thread.sleep(properties.getRetryDelay() * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry delay", ie);
                    }
                }
            }
        }

        log.error("Create charge failed after {} attempts for application: {}", properties.getMaxRetries(), applicationId);
        throw new RuntimeException("Failed to create charge after " + properties.getMaxRetries() + " attempts", lastException);
    }

    /**
     * Verifies transaction status.
     */
    @Override
    public SterumVerifyResponse verificarTransaccion(String transactionId) {
        log.info("Verifying transaction status: {}", transactionId);
        ensureTokenIsValid();
        return verifyTransactionWithRetry(transactionId);
    }

    /**
     * Verifies transaction with retry logic.
     */
    private SterumVerifyResponse verifyTransactionWithRetry(String transactionId) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxRetries()) {
            try {
                log.debug("Sending verify transaction request (attempt {})", attempt + 1);

                ResponseEntity<SterumVerifyResponse> response = restClient.get()
                        .uri(properties.getBaseUrl() + "/api/v1/transactions/" + transactionId + "/verify")
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(SterumVerifyResponse.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Verify transaction failed with status: {}", response.getStatusCode());
                    throw new RuntimeException("Verify transaction failed: " + response.getStatusCode());
                }

                SterumVerifyResponse verifyResponse = response.getBody();
                log.info("Transaction {} status: {}", transactionId, verifyResponse.getStatus());
                return verifyResponse;

            } catch (HttpClientErrorException.Unauthorized e) {
                log.warn("Token unauthorized (401), refreshing token and retrying");
                refrescarToken();
                attempt++;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Verify transaction attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                
                if (attempt < properties.getMaxRetries()) {
                    try {
                        Thread.sleep(properties.getRetryDelay() * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry delay", ie);
                    }
                }
            }
        }

        log.error("Verify transaction failed after {} attempts for transaction: {}", 
                properties.getMaxRetries(), transactionId);
        throw new RuntimeException("Failed to verify transaction after " + properties.getMaxRetries() + " attempts", lastException);
    }

    /**
     * Cancels a transaction.
     */
    @Override
    public SterumCancelResponse cancelarTransaccion(String transactionId) {
        log.info("Canceling transaction: {}", transactionId);
        ensureTokenIsValid();
        return cancelTransactionWithRetry(transactionId);
    }

    /**
     * Cancels transaction with retry logic.
     */
    private SterumCancelResponse cancelTransactionWithRetry(String transactionId) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxRetries()) {
            try {
                log.debug("Sending cancel transaction request (attempt {})", attempt + 1);

                ResponseEntity<SterumCancelResponse> response = restClient.post()
                        .uri(properties.getBaseUrl() + "/api/v1/transactions/" + transactionId + "/cancel")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(SterumCancelResponse.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Cancel transaction failed with status: {}", response.getStatusCode());
                    throw new RuntimeException("Cancel transaction failed: " + response.getStatusCode());
                }

                SterumCancelResponse cancelResponse = response.getBody();
                log.info("Transaction {} canceled successfully. New status: {}", 
                        transactionId, cancelResponse.getStatus());
                return cancelResponse;

            } catch (HttpClientErrorException.Unauthorized e) {
                log.warn("Token unauthorized (401), refreshing token and retrying");
                refrescarToken();
                attempt++;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Cancel transaction attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                
                if (attempt < properties.getMaxRetries()) {
                    try {
                        Thread.sleep(properties.getRetryDelay() * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry delay", ie);
                    }
                }
            }
        }

        log.error("Cancel transaction failed after {} attempts for transaction: {}", 
                properties.getMaxRetries(), transactionId);
        throw new RuntimeException("Failed to cancel transaction after " + properties.getMaxRetries() + " attempts", lastException);
    }

    /**
     * Converts currency using live exchange rates.
     */
    @Override
    public SterumConversionResponse convertirMoneda(String fromCurrency, String toCurrency, Double amount) {
        log.info("Converting {} {} to {}", amount, fromCurrency, toCurrency);
        ensureTokenIsValid();
        return convertCurrencyWithRetry(fromCurrency, toCurrency, amount);
    }

    /**
     * Converts currency with retry logic.
     */
    private SterumConversionResponse convertCurrencyWithRetry(String fromCurrency, String toCurrency, Double amount) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < properties.getMaxRetries()) {
            try {
                log.debug("Sending currency conversion request (attempt {})", attempt + 1);

                String url = String.format(
                        "%s/api/v1/currency/convert?country=%s&from=%s&to=%s&amount=%s",
                        properties.getBaseUrl(),
                        properties.getDefaultCountry(),
                        fromCurrency,
                        toCurrency,
                        amount
                );

                ResponseEntity<SterumConversionResponse> response = restClient.get()
                        .uri(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(SterumConversionResponse.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Currency conversion failed with status: {}", response.getStatusCode());
                    throw new RuntimeException("Currency conversion failed: " + response.getStatusCode());
                }

                SterumConversionResponse conversionResponse = response.getBody();
                log.info("Conversion result: {} {} = {} {} (rate: {})", 
                        amount, fromCurrency, conversionResponse.getConvertedAmount(), toCurrency, 
                        conversionResponse.getExchangeRate());
                return conversionResponse;

            } catch (HttpClientErrorException.Unauthorized e) {
                log.warn("Token unauthorized (401), refreshing token and retrying");
                refrescarToken();
                attempt++;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Currency conversion attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                
                if (attempt < properties.getMaxRetries()) {
                    try {
                        Thread.sleep(properties.getRetryDelay() * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry delay", ie);
                    }
                }
            }
        }

        log.error("Currency conversion failed after {} attempts", properties.getMaxRetries());
        throw new RuntimeException("Failed to convert currency after " + properties.getMaxRetries() + " attempts", lastException);
    }

    /**
     * Gets the current JWT token.
     */
    @Override
    public String obtenerTokenActual() {
        ensureTokenIsValid();
        return jwtToken;
    }

    /**
     * Checks if current token is expired.
     */
    @Override
    public boolean esTokenExpirado() {
        if (jwtToken == null) {
            return true;
        }
        long currentTime = Instant.now().getEpochSecond();
        long timeUntilExpiration = tokenExpirationTime - currentTime;
        return timeUntilExpiration < properties.getTokenRefreshThreshold() / 1000;
    }

    /**
     * Refreshes the JWT token.
     */
    @Override
    public StereumAuthResponse refrescarToken() {
        log.info("Refreshing JWT token");
        jwtToken = null; // Clear current token
        tokenExpirationTime = 0;
        return obtienTokenAutenticacion();
    }

    /**
     * Ensures token is valid, refreshes if necessary.
     */
    private void ensureTokenIsValid() {
        if (esTokenExpirado()) {
            log.info("Token expired or missing, refreshing...");
            refrescarToken();
        }
    }

    /**
     * Creates RestClient with configured timeouts.
     */
    private RestClient createRestClient() {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    /**
     * Configures HTTP request factory with timeouts.
     */
    private org.springframework.http.client.HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory = 
                new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(Duration.ofMillis(properties.getConnectTimeout()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeout()));
        return factory;
    }

    /**
     * Generates unique idempotency key.
     */
    private String generateIdempotencyKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
