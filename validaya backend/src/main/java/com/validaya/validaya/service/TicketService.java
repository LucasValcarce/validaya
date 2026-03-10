package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.TicketDto;

public interface TicketService {

    TicketDto.Response getById(Long id);

    TicketDto.Response getByCode(String code);

    TicketDto.Response getByApplication(Long applicationId);

    TicketDto.Response generateForApplication(Long applicationId);

    void expireOverdue();
}