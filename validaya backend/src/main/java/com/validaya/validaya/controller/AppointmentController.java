package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.AppointmentDto;
import com.validaya.validaya.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentDto.Response>> schedule(
            @Valid @RequestBody AppointmentDto.ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.schedule(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.getById(id)));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<AppointmentDto.Response>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.getByTicket(ticketId)));
    }

    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<AppointmentDto.SlotResponse>>> getAvailableSlots(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.getAvailableSlots(branchId, startDate, endDate)));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<AppointmentDto.Response>> complete(
            @PathVariable Long id,
            @RequestParam(required = false) String staffNotes,
            @RequestParam Long staffId) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.complete(id, staffNotes, staffId)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentDto.Response>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(appointmentService.cancel(id)));
    }
}