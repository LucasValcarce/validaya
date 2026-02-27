package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.TicketDto;
import com.validaya.validaya.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<TicketDto.Response>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getByCode(code)));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<TicketDto.Response>> getByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.getByApplication(applicationId)));
    }
}