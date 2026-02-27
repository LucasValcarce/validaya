package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.AppointmentDto;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentDto.Response schedule(AppointmentDto.ScheduleRequest request);

    AppointmentDto.Response getById(Long id);

    AppointmentDto.Response getByTicket(Long ticketId);

    List<AppointmentDto.SlotResponse> getAvailableSlots(Long branchId, LocalDate startDate, LocalDate endDate);

    AppointmentDto.Response complete(Long id, String staffNotes, Long staffId);

    AppointmentDto.Response cancel(Long id);
}