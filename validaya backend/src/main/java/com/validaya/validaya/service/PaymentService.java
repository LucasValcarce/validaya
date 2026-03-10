package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.PaymentDto;

/**
 * Service interface for payment operations.
 * Handles payment creation, retrieval, and webhook callbacks.
 */
public interface PaymentService {

    /**
     * Initiates a payment for an application.
     *
     * @param request payment initiation request
     * @return payment response with transaction details
     */
    PaymentDto.Response initiate(PaymentDto.InitiateRequest request);

    /**
     * Retrieves payment by application ID.
     *
     * @param applicationId application ID
     * @return payment response
     */
    PaymentDto.Response getByApplication(Long applicationId);

    /**
     * Handles gateway callback from Stereum Pay webhook.
     *
     * @param callback gateway callback with transaction status
     * @return updated payment response
     */
    PaymentDto.Response handleGatewayCallback(PaymentDto.GatewayCallback callback);

    /**
     * Retrieves payment by ID.
     *
     * @param id payment ID
     * @return payment response
     */
    PaymentDto.Response getById(Long id);

    /**
     * Verifies transaction status with Stereum Pay.
     *
     * @param paymentId payment ID
     * @return updated payment response
     */
    PaymentDto.Response verifyTransaction(Long paymentId);

    /**
     * Cancels a payment.
     *
     * @param paymentId payment ID
     * @return updated payment response
     */
    PaymentDto.Response cancelPayment(Long paymentId);
}