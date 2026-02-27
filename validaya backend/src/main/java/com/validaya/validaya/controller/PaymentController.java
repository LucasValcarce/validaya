package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.PaymentDto;
import com.validaya.validaya.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> initiate(
            @Valid @RequestBody PaymentDto.InitiateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.initiate(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getById(id)));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByApplication(applicationId)));
    }

    /**
     * Endpoint de callback para el gateway de pagos (QR Simple, Tigo Money, etc.)
     * No requiere autenticación - la seguridad la provee la firma del gateway.
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> gatewayCallback(
            @RequestBody PaymentDto.GatewayCallback callback) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.handleGatewayCallback(callback)));
    }
}