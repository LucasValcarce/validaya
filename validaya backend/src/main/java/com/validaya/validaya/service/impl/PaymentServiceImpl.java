package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Application;
import com.validaya.validaya.entity.Payment;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.entity.enums.ApplicationStatus;
import com.validaya.validaya.entity.enums.PaymentStatus;
import com.validaya.validaya.repository.ApplicationRepository;
import com.validaya.validaya.repository.PaymentRepository;
import com.validaya.validaya.service.PaymentService;
import com.validaya.validaya.service.TicketService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketService ticketService;

    @Override
    @Transactional
    public PaymentDto.Response initiate(PaymentDto.InitiateRequest request) {
        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada"));

        if (app.getStatus() != ApplicationStatus.payment_pending) {
            throw new IllegalStateException("La solicitud no está en estado de pago pendiente");
        }

        // Calcular desglose
        BigDecimal total = app.getTotalAmount();
        BigDecimal platformFee = app.getProcedure().getPlatformFee();
        BigDecimal institutionAmount = total.subtract(platformFee);

        Payment payment = Payment.builder()
                .application(app)
                .transactionId("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .amount(total)
                .platformFee(platformFee)
                .institutionAmount(institutionAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.pending)
                .gateway("SIMULATED")
                .build();

        payment = paymentRepository.save(payment);
        log.info("Pago iniciado: {} para solicitud id={}", payment.getTransactionId(), app.getId());

        return MapperUtil.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto.Response getByApplication(Long applicationId) {
        Payment payment = paymentRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para solicitud: " + applicationId));
        return MapperUtil.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentDto.Response handleGatewayCallback(PaymentDto.GatewayCallback callback) {
        Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + callback.getTransactionId()));

        String status = callback.getStatus() == null ? "" : callback.getStatus();

        if ("APPROVED".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
            payment.setPaymentStatus(PaymentStatus.completed);
            payment.setPaidAt(LocalDateTime.now());
            payment.setGatewayResponse(Map.of(
                    "externalReference", callback.getExternalReference(),
                    "raw", callback.getGatewayResponse(),
                    "status", callback.getStatus()
            ));

            Application app = payment.getApplication();
            app.setStatus(ApplicationStatus.payment_confirmed);
            applicationRepository.save(app);

            paymentRepository.save(payment);

            ticketService.generateForApplication(app.getId());
            log.info("Pago confirmado y ticket generado para solicitud id={}", app.getId());
        } else {
            payment.setPaymentStatus(PaymentStatus.failed);
            payment.setGatewayResponse(Map.of(
                    "externalReference", callback.getExternalReference(),
                    "raw", callback.getGatewayResponse(),
                    "status", callback.getStatus()
            ));
            paymentRepository.save(payment);
        }

        return MapperUtil.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto.Response getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + id));
        return MapperUtil.toPaymentResponse(payment);
    }
}