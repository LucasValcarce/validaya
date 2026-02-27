package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.PaymentDto;

public interface PaymentService {

    PaymentDto.Response initiate(PaymentDto.InitiateRequest request);

    PaymentDto.Response getByApplication(Long applicationId);

    PaymentDto.Response handleGatewayCallback(PaymentDto.GatewayCallback callback);

    PaymentDto.Response getById(Long id);
}