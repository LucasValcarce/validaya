package com.validaya.validaya.service.impl;

import com.validaya.validaya.config.SterumPayProperties;
import com.validaya.validaya.entity.Application;
import com.validaya.validaya.entity.Payment;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.entity.enums.ApplicationStatus;
import com.validaya.validaya.entity.enums.PaymentMethod;
import com.validaya.validaya.entity.enums.PaymentStatus;
import com.validaya.validaya.integracion.SterumPayService;
import com.validaya.validaya.integracion.impl.dtos.*;
import com.validaya.validaya.repository.ApplicationRepository;
import com.validaya.validaya.repository.PaymentRepository;
import com.validaya.validaya.service.NotificationService;
import com.validaya.validaya.service.PaymentService;
import com.validaya.validaya.service.TicketService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service implementation for payment processing.
 * Handles payment creation, verification, and webhook callbacks.
 * Integrates with Stereum Pay API for payment processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketService ticketService;
    private final SterumPayService sterumPayService;
    private final NotificationService notificationService;
    private final SterumPayProperties sterumPayProperties;

    @Override
    @Transactional
    public PaymentDto.Response initiate(PaymentDto.InitiateRequest request) {
        log.info("Initiating payment for application: {}", request.getApplicationId());

        // Retrieve application
        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> {
                    log.error("Application not found: {}", request.getApplicationId());
                    return new EntityNotFoundException("Aplicación no encontrada");
                });

        // Validate application status
        if (app.getStatus() != ApplicationStatus.payment_pending) {
            log.error("Application {} not in payment_pending status", request.getApplicationId());
            throw new IllegalStateException("La solicitud no está en estado de pago pendiente");
        }

        // Check if payment already exists
        if (paymentRepository.findByApplicationId(request.getApplicationId()).isPresent()) {
            log.error("Payment already exists for application: {}", request.getApplicationId());
            throw new IllegalStateException("Ya existe un pago para esta solicitud");
        }

        // Calculate payment breakdown
        BigDecimal totalAmount = app.getTotalAmount();
        BigDecimal platformFee = app.getProcedure().getPlatformFee() != null 
                ? app.getProcedure().getPlatformFee() 
                : BigDecimal.ZERO;
        BigDecimal institutionAmount = totalAmount.subtract(platformFee);

        // Create payment entity
        String idempotencyKey = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .application(app)
                .amount(totalAmount)
                .platformFee(platformFee)
                .institutionAmount(institutionAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.pending)
                .gateway("STEREUM_PAY")
                .idempotencyKey(idempotencyKey)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created: {}", payment.getId());

        // Initiate charge with Stereum Pay
        try {
            // Build customer data
            User user = app.getUser();
            SterumCustomerData customer = SterumCustomerData.builder()
                    .name(user.getFullName().split(" ")[0])
                    .lastname(user.getFullName().contains(" ") 
                            ? user.getFullName().substring(user.getFullName().indexOf(" ") + 1)
                            : "")
                    .documentNumber(user.getIdentification())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .country(sterumPayProperties.getDefaultCountry())
                    .build();

            // Build charge request
            SterumCreateChargeRequest chargeRequest = SterumCreateChargeRequest.builder()
                    .country(sterumPayProperties.getDefaultCountry())
                    .amount(totalAmount.toPlainString())
                    .network(sterumPayProperties.getDefaultNetwork())
                    .currency(sterumPayProperties.getDefaultBlockchainCurrency())
                    .idempotencyKey(idempotencyKey)
                    .chargeReason("Pago de solicitud APP-" + app.getId())
                    .callback(buildCallbackUrl(payment.getId()))
                    .customer(customer)
                    .reference("APP-" + app.getId())
                    .build();

            // Send to Stereum Pay
            SterumCreateChargeResponse sterumResponse = sterumPayService.crearCargoCobro(
                    chargeRequest,
                    app.getId()
            );

            // Update payment with response data
            payment.setTransactionId(sterumResponse.getId());
            payment.setQrCodeBase64(sterumResponse.getQrBase64());
            payment.setPaymentLink(sterumResponse.getPaymentLink());
            
            // Store expiration time
            if (sterumResponse.getExpirationTime() != null) {
                LocalDateTime expirationTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(sterumResponse.getExpirationTime()),
                        ZoneId.systemDefault()
                );
                payment.setExpiresAt(expirationTime);
            }

            // Store gateway response
            Map<String, Object> gatewayResponse = new HashMap<>();
            gatewayResponse.put("stereum_id", sterumResponse.getId());
            gatewayResponse.put("status", sterumResponse.getTransactionStatus());
            gatewayResponse.put("payment_url", sterumResponse.getPaymentLink());
            gatewayResponse.put("qr_base64", sterumResponse.getQrBase64());
            gatewayResponse.put("network", sterumResponse.getNetwork());
            gatewayResponse.put("currency", sterumResponse.getCurrency());
            payment.setGatewayResponse(gatewayResponse);

            payment = paymentRepository.save(payment);
            log.info("Payment initiated successfully. Stereum ID: {}, Payment ID: {}", 
                    sterumResponse.getId(), payment.getId());

        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.failed);
            payment.setLastError(e.getMessage());
            paymentRepository.save(payment);
            
            log.error("Error initiating charge with Stereum: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo procesar el pago: " + e.getMessage());
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto.Response getByApplication(Long applicationId) {
        log.info("Retrieving payment for application: {}", applicationId);
        
        Payment payment = paymentRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> {
                    log.error("Payment not found for application: {}", applicationId);
                    return new EntityNotFoundException("Pago no encontrado para solicitud: " + applicationId);
                });
        
        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Retrieves payment by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentDto.Response getById(Long id) {
        log.info("Retrieving payment: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", id);
                    return new EntityNotFoundException("Pago no encontrado: " + id);
                });
        
        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Handles webhook callback from Stereum Pay.
     * Updates payment status based on transaction status.
     */
    @Override
    @Transactional
    public PaymentDto.Response handleGatewayCallback(PaymentDto.GatewayCallback callback) {
        log.info("Processing gateway callback for transaction: {}", callback.getTransactionId());

        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                .orElseThrow(() -> {
                    log.error("Payment not found for transaction: {}", callback.getTransactionId());
                    return new EntityNotFoundException("Pago no encontrado: " + callback.getTransactionId());
                });

        String status = (callback.getStatus() != null ? callback.getStatus() : "").toUpperCase();
        
        log.info("Processing payment {} with status: {}", payment.getId(), status);

        // Handle different transaction statuses
        switch (status) {
            case "PAGADO":
            case "COMPLETED":
            case "APPROVED":
                handleSuccessfulPayment(payment);
                break;
                
            case "PENDIENTE":
            case "INICIADO":
                handlePendingPayment(payment);
                break;
                
            case "CANCELADO":
            case "CANCELLED":
                handleCancelledPayment(payment);
                break;
                
            case "ERROR":
            default:
                handleFailedPayment(payment, callback);
                break;
        }

        payment = paymentRepository.save(payment);
        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Handles successful payment.
     */
    private void handleSuccessfulPayment(Payment payment) {
        log.info("Processing successful payment: {}", payment.getId());
        
        payment.setPaymentStatus(PaymentStatus.completed);
        payment.setPaidAt(LocalDateTime.now());
        
        // Update application status
        Application app = payment.getApplication();
        app.setStatus(ApplicationStatus.payment_confirmed);
        applicationRepository.save(app);

        // Generate ticket
        try {
            ticketService.generateForApplication(app.getId());
            log.info("Ticket generated for application: {}", app.getId());
        } catch (Exception e) {
            log.error("Error generating ticket: {}", e.getMessage(), e);
            // Don't fail the payment process if ticket generation fails
        }

        // Send notification to user
        try {
            notificationService.send(
                    app.getUser().getId(),
                    "Pago confirmado",
                    "Tu pago para APP-" + app.getId() + " ha sido confirmado. Tu cita ha sido agendada.",
                    com.validaya.validaya.entity.enums.NotificationChannel.in_app,
                    "payment",
                    payment.getId()
            );
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }

        log.info("Payment confirmed and ticket generated for application: {}", app.getId());
    }

    /**
     * Handles pending payment.
     */
    private void handlePendingPayment(Payment payment) {
        log.info("Payment still pending: {}", payment.getId());
        payment.setPaymentStatus(PaymentStatus.pending);
    }

    /**
     * Handles cancelled payment.
     */
    private void handleCancelledPayment(Payment payment) {
        log.info("Payment cancelled: {}", payment.getId());
        
        payment.setPaymentStatus(PaymentStatus.cancelled);
        
        // Notify user
        try {
            notificationService.send(
                    payment.getApplication().getUser().getId(),
                    "Pago cancelado",
                    "Tu pago para APP-" + payment.getApplication().getId() + " ha sido cancelado.",
                    com.validaya.validaya.entity.enums.NotificationChannel.in_app,
                    "payment",
                    payment.getId()
            );
        } catch (Exception e) {
            log.error("Error sending cancellation notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles failed payment.
     */
    private void handleFailedPayment(Payment payment, PaymentDto.GatewayCallback callback) {
        log.warn("Payment failed: {}", payment.getId());
        
        payment.setPaymentStatus(PaymentStatus.failed);
        payment.setLastError(callback.getStatus());

        // Update gateway response with error info
        Map<String, Object> response = payment.getGatewayResponse() != null 
                ? payment.getGatewayResponse()
                : new HashMap<>();
        response.put("status", callback.getStatus());
        response.put("error", callback.getGatewayResponse());
        response.put("error_at", LocalDateTime.now());
        payment.setGatewayResponse(response);

        // Notify user
        try {
            notificationService.send(
                    payment.getApplication().getUser().getId(),
                    "Pago rechazado",
                    "Tu pago para APP-" + payment.getApplication().getId() + " fue rechazado. Por favor intenta nuevamente.",
                    com.validaya.validaya.entity.enums.NotificationChannel.in_app,
                    "payment",
                    payment.getId()
            );
        } catch (Exception e) {
            log.error("Error sending failure notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds callback URL for Stereum webhooks.
     */
    private String buildCallbackUrl(Long paymentId) {
        // This should be configured in properties
        return "https://your-domain.com/api/v1/payments/webhook";
    }

    /**
     * Verifies transaction status with Stereum Pay.
     * Can be called to check payment status without waiting for webhook.
     */
    public PaymentDto.Response verifyTransaction(Long paymentId) {
        log.info("Verifying transaction for payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + paymentId));

        try {
            SterumVerifyResponse verifyResponse = sterumPayService.verificarTransaccion(
                    payment.getTransactionId()
            );

            // Update payment with verification response
            payment.setGatewayResponse(convertVerifyResponseToMap(verifyResponse));
            
            // Update status based on verification
            switch (verifyResponse.getStatus().toUpperCase()) {
                case "PAGADO":
                    if (payment.getPaymentStatus() != PaymentStatus.completed) {
                        handleSuccessfulPayment(payment);
                    }
                    break;
                case "CANCELADO":
                    payment.setPaymentStatus(PaymentStatus.cancelled);
                    break;
                case "ERROR":
                    payment.setPaymentStatus(PaymentStatus.failed);
                    payment.setLastError(verifyResponse.getError());
                    break;
                default:
                    payment.setPaymentStatus(PaymentStatus.pending);
            }

            payment = paymentRepository.save(payment);
            log.info("Transaction verification completed for payment: {}", paymentId);
            
        } catch (Exception e) {
            log.error("Error verifying transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar la transacción: " + e.getMessage());
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Cancels a payment with Stereum Pay.
     */
    public PaymentDto.Response cancelPayment(Long paymentId) {
        log.info("Canceling payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + paymentId));

        try {
            SterumCancelResponse cancelResponse = sterumPayService.cancelarTransaccion(
                    payment.getTransactionId()
            );

            payment.setPaymentStatus(PaymentStatus.cancelled);
            payment.setGatewayResponse(convertCancelResponseToMap(cancelResponse));
            
            payment = paymentRepository.save(payment);
            log.info("Payment cancelled successfully: {}", paymentId);
            
        } catch (Exception e) {
            log.error("Error canceling payment: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cancelar el pago: " + e.getMessage());
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Converts SterumVerifyResponse to Map for storage.
     */
    private Map<String, Object> convertVerifyResponseToMap(SterumVerifyResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", response.getStatus());
        map.put("amount", response.getAmount());
        map.put("currency", response.getCurrency());
        map.put("paid_amount", response.getPaidAmount());
        map.put("paid_currency", response.getPaidCurrency());
        map.put("tx_hash", response.getTxHash());
        map.put("confirmations", response.getConfirmations());
        map.put("paid_at", response.getPaidAt());
        if (response.getError() != null) {
            map.put("error", response.getError());
        }
        return map;
    }

    /**
     * Converts SterumCancelResponse to Map for storage.
     */
    private Map<String, Object> convertCancelResponseToMap(SterumCancelResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", response.getStatus());
        map.put("message", response.getMessage());
        map.put("cancelled_at", response.getCancelledAt());
        return map;
    }
}