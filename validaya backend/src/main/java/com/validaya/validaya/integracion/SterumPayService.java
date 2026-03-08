package com.validaya.validaya.integracion;

import com.validaya.validaya.integracion.impl.dtos.*;

/**
 * Service interface for Stereum Pay API integration.
 * Defines all operations for interacting with Stereum Pay gateway.
 */
public interface SterumPayService {

    /**
     * Obtains JWT token from Stereum Pay API.
     * Token is valid for 8 hours.
     *
     * @return StereumAuthResponse containing access token
     */
    StereumAuthResponse obtienTokenAutenticacion();

    /**
     * Creates a charge in Stereum Pay.
     * Generates QR code and payment link for customer.
     *
     * @param request charge request with customer and amount details
     * @param applicationId application ID for tracking
     * @return charge response with transaction ID, QR code, and payment link
     */
    SterumCreateChargeResponse crearCargoCobro(SterumCreateChargeRequest request, Long applicationId);

    /**
     * Verifies the current status of a transaction.
     *
     * @param transactionId the Stereum transaction ID
     * @return verification response with current status
     */
    SterumVerifyResponse verificarTransaccion(String transactionId);

    /**
     * Cancels a pending transaction.
     *
     * @param transactionId the Stereum transaction ID to cancel
     * @return cancellation response
     */
    SterumCancelResponse cancelarTransaccion(String transactionId);

    /**
     * Converts currency using Stereum's exchange rates.
     *
     * @param fromCurrency source currency code (e.g., BOB)
     * @param toCurrency target currency code (e.g., USDT)
     * @param amount amount to convert
     * @return conversion response with converted amount and exchange rate
     */
    SterumConversionResponse convertirMoneda(String fromCurrency, String toCurrency, Double amount);

    /**
     * Gets the current JWT token (for testing/debugging).
     * Token is automatically refreshed when needed.
     *
     * @return the current valid JWT token
     */
    String obtenerTokenActual();

    /**
     * Checks if the current JWT token is expired.
     *
     * @return true if token is expired or missing, false otherwise
     */
    boolean esTokenExpirado();

    /**
     * Forces token refresh.
     * Useful when token might have been invalidated.
     *
     * @return new StereumAuthResponse
     */
    StereumAuthResponse refrescarToken();
}
