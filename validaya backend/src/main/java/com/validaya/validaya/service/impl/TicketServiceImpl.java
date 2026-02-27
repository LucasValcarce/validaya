package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Application;
import com.validaya.validaya.entity.Ticket;
import com.validaya.validaya.entity.dto.TicketDto;
import com.validaya.validaya.entity.enums.TicketStatus;
import com.validaya.validaya.repository.ApplicationRepository;
import com.validaya.validaya.repository.TicketRepository;
import com.validaya.validaya.service.TicketService;
import com.validaya.validaya.utils.MapperUtil;
import com.validaya.validaya.utils.QrUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ApplicationRepository applicationRepository;
    private final QrUtil qrUtil;

    @Override
    public TicketDto.Response getById(Long id) {
        return MapperUtil.toTicketResponse(findOrThrow(id));
    }

    @Override
    public TicketDto.Response getByCode(String code) {
        return MapperUtil.toTicketResponse(ticketRepository.findByTicketCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + code)));
    }

    @Override
    public TicketDto.Response getByApplication(Long applicationId) {
        return MapperUtil.toTicketResponse(ticketRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado para solicitud: " + applicationId)));
    }

    @Override
    @Transactional
    public TicketDto.Response generateForApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + applicationId));

        // Verificar que no exista ya un ticket
        if (ticketRepository.findByApplicationId(applicationId).isPresent()) {
            throw new IllegalStateException("Ya existe un ticket para esta solicitud");
        }

        String ticketCode = generateUniqueCode();
        String qrPayload = qrUtil.generatePayload(ticketCode, applicationId, "APP-" + applicationId);

        Ticket ticket = Ticket.builder()
                .application(app)
                .ticketCode(ticketCode)
                .status(TicketStatus.active)
                .qrPayload(qrPayload)
                .expiryAt(LocalDateTime.now().plusDays(30))
                .build();

        ticket = ticketRepository.save(ticket);
        log.info("Ticket generado: {} para solicitud: {}", ticketCode, applicationId);
        return MapperUtil.toTicketResponse(ticket);
    }

    @Override
    @Transactional
    public void expireOverdue() {
        List<Ticket> overdue = ticketRepository.findByStatusAndExpiryAtBefore(
                TicketStatus.active, LocalDateTime.now());
        overdue.forEach(t -> t.setStatus(TicketStatus.expired));
        ticketRepository.saveAll(overdue);
        log.info("Tickets expirados: {}", overdue.size());
    }

    private Ticket findOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado: " + id));
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder("TK-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            code = sb.toString();
        } while (ticketRepository.findByTicketCode(code).isPresent());
        return code;
    }
}