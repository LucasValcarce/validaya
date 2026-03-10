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
import com.validaya.validaya.service.UserDocumentService;
import com.validaya.validaya.utils.MapperUtil;
import com.validaya.validaya.entity.UserDocument;
import com.validaya.validaya.entity.enums.DocumentSource;
import com.validaya.validaya.entity.enums.DocumentStatus;
import com.validaya.validaya.entity.enums.VerificationStatus;
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
    private final UserDocumentService userDocumentService;
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
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        return MapperUtil.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto.Response getById(Long id) {
        log.info("Retrieving payment: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
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

        Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        String status = (callback.getStatus() != null ? callback.getStatus() : "").toUpperCase();
        
        switch (status) {
            case "PAGADO":
            case "COMPLETED":
            case "APPROVED":
                processSuccessfulPayment(payment);
                break;
            case "PENDIENTE":
            case "INICIADO":
                payment.setPaymentStatus(PaymentStatus.pending);
                break;
            case "CANCELADO":
            case "CANCELLED":
                processFailedPayment(payment, "Pago cancelado");
                break;
            default:
                processFailedPayment(payment, callback.getStatus());
        }

        payment = paymentRepository.save(payment);
        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Processes successful payment.
     * Creates the output document as active and verified for immediate platform use.
     * Ticket generation is optional and separate from payment completion.
     */
    private void processSuccessfulPayment(Payment payment) {
        log.info("Processing approved payment: {}", payment.getId());
        payment.setPaymentStatus(PaymentStatus.completed);
        payment.setPaidAt(LocalDateTime.now());

        Application app = payment.getApplication();
        app.setStatus(ApplicationStatus.payment_confirmed);
        applicationRepository.save(app);

        try {
            // Create the output document as active and verified for immediate platform use
            log.info("Procedure for application {} has outputDocumentType: {}", 
                    app.getId(), app.getProcedure().getOutputDocumentType());
            
            if (app.getProcedure().getOutputDocumentType() != null) {
                UserDocument userDoc = UserDocument.builder()
                        .user(app.getUser())
                        .documentType(app.getProcedure().getOutputDocumentType())
                        .issuingInstitution(app.getProcedure().getInstitution())
                        .status(DocumentStatus.active)
                        .verificationStatus(VerificationStatus.verified)
                        .source(DocumentSource.platform_generated)
                        .verifiedAt(LocalDateTime.now())
                        .build();
                
                userDocumentService.register(userDoc);
                log.info("Active document created for user {} after successful payment for application {}", 
                        app.getUser().getId(), app.getId());
            } else {
                log.warn("No outputDocumentType configured for procedure of application {}. Document not created.", 
                        app.getId());
            }
            
            notificationService.send(
                    app.getUser().getId(),
                    "Pago confirmado",
                    "Tu pago para APP-" + app.getId() + " ha sido confirmado. Tu documento ya está disponible en la plataforma.",
                    com.validaya.validaya.entity.enums.NotificationChannel.in_app,
                    "payment",
                    payment.getId()
            );
        } catch (Exception e) {
            log.error("Error in post-payment processing for payment {}: {}", payment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Processes failed payment.
     */
    private void processFailedPayment(Payment payment, String errorReason) {
        log.warn("Processing failed payment: {} - {}", payment.getId(), errorReason);
        payment.setPaymentStatus(PaymentStatus.failed);
        payment.setLastError(errorReason);

        try {
            notificationService.send(
                    payment.getApplication().getUser().getId(),
                    "Pago rechazado",
                    "Tu pago para APP-" + payment.getApplication().getId() + " fue rechazado.",
                    com.validaya.validaya.entity.enums.NotificationChannel.in_app,
                    "payment",
                    payment.getId()
            );
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
        }
    }

    /**
     * Verifies transaction status with Stereum Pay.
     */
    public PaymentDto.Response verifyTransaction(Long paymentId) {
        log.info("Verifying transaction for payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        try {
            SterumVerifyResponse response = sterumPayService.verificarTransaccion(payment.getTransactionId());
            updatePaymentStatus(payment, response.getStatus());
            payment.setGatewayResponse(convertToMap(response));
            payment = paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error verifying transaction: {}", e.getMessage());
            throw new RuntimeException("Error al verificar la transacción");
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Cancels a payment with Stereum Pay.
     */
    public PaymentDto.Response cancelPayment(Long paymentId) {
        log.info("Canceling payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

        try {
            SterumCancelResponse response = sterumPayService.cancelarTransaccion(payment.getTransactionId());
            payment.setPaymentStatus(PaymentStatus.cancelled);
            payment.setGatewayResponse(convertToMap(response));
            payment = paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error canceling payment: {}", e.getMessage());
            throw new RuntimeException("Error al cancelar el pago");
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    /**
     * Updates payment status based on transaction response.
     */
    private void updatePaymentStatus(Payment payment, String transactionStatus) {
        switch (transactionStatus.toUpperCase()) {
            case "PAGADO":
                processSuccessfulPayment(payment);
                break;
            case "CANCELADO":
                payment.setPaymentStatus(PaymentStatus.cancelled);
                break;
            case "ERROR":
                payment.setPaymentStatus(PaymentStatus.failed);
                break;
            default:
                payment.setPaymentStatus(PaymentStatus.pending);
        }
    }

    /**
     * Converts response objects to map for storage.
     */
    private Map<String, Object> convertToMap(Object response) {
        if (response == null) return new HashMap<>();
        
        Map<String, Object> map = new HashMap<>();
        if (response instanceof SterumVerifyResponse) {
            SterumVerifyResponse r = (SterumVerifyResponse) response;
            map.put("status", r.getStatus());
            map.put("paid_amount", r.getPaidAmount());
            map.put("tx_hash", r.getTxHash());
        } else if (response instanceof SterumCancelResponse) {
            SterumCancelResponse r = (SterumCancelResponse) response;
            map.put("status", r.getStatus());
            map.put("message", r.getMessage());
        }
        return map;
    }
}