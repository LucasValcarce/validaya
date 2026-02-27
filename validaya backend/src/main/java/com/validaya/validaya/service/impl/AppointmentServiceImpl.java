package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Appointment;
import com.validaya.validaya.entity.AppointmentSlot;
import com.validaya.validaya.entity.Branch;
import com.validaya.validaya.entity.Ticket;
import com.validaya.validaya.entity.dto.AppointmentDto;
import com.validaya.validaya.entity.enums.AppointmentStatus;
import com.validaya.validaya.entity.enums.TicketStatus;
import com.validaya.validaya.repository.AppointmentRepository;
import com.validaya.validaya.repository.AppointmentSlotRepository;
import com.validaya.validaya.repository.BranchRepository;
import com.validaya.validaya.repository.TicketRepository;
import com.validaya.validaya.service.AppointmentService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final TicketRepository ticketRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public AppointmentDto.Response schedule(AppointmentDto.ScheduleRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado"));

        if (ticket.getStatus() != TicketStatus.active) {
            throw new IllegalStateException("El ticket no está activo");
        }
        if (appointmentRepository.findByTicketId(ticket.getId()).isPresent()) {
            throw new IllegalStateException("Ya existe una cita para este ticket");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        AppointmentSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Horario no disponible"));

        if (Boolean.TRUE.equals(slot.getIsBlocked()) || slot.getReservedCount() >= slot.getMaxCapacity()) {
            throw new IllegalStateException("El horario seleccionado no está disponible");
        }

        slot.setReservedCount(slot.getReservedCount() + 1);
        slotRepository.save(slot);

        // Ticket expira poco después de la hora reservada (no hay endTime en la entidad)
        ticket.setExpiryAt(slot.getSlotDate().atTime(slot.getSlotTime()).plusHours(4));
        ticketRepository.save(ticket);

        Appointment appointment = Appointment.builder()
                .ticket(ticket)
                .branch(branch)
                .appointmentDate(slot.getSlotDate())
                .appointmentTime(slot.getSlotTime())
                .status(AppointmentStatus.scheduled)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Cita agendada para ticket={} en sucursal={}", ticket.getTicketCode(), branch.getName());
        return MapperUtil.toAppointmentResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto.Response getById(Long id) {
        return MapperUtil.toAppointmentResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto.Response getByTicket(Long ticketId) {
        return MapperUtil.toAppointmentResponse(
                appointmentRepository.findByTicketId(ticketId)
                        .orElseThrow(() -> new EntityNotFoundException("Cita no encontrada para ticket: " + ticketId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto.SlotResponse> getAvailableSlots(Long branchId, LocalDate startDate, LocalDate endDate) {
        return slotRepository.findByBranchIdAndSlotDateBetweenAndIsBlockedFalse(branchId, startDate, endDate)
                .stream()
                .filter(s -> s.getReservedCount() < s.getMaxCapacity())
                .map(MapperUtil::toSlotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDto.Response complete(Long id, String staffNotes, Long staffId) {
        Appointment appointment = findOrThrow(id);

        appointment.setStatus(AppointmentStatus.attended);

        Ticket ticket = appointment.getTicket();
        ticket.setStatus(TicketStatus.used);
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return MapperUtil.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentDto.Response cancel(Long id) {
        Appointment appointment = findOrThrow(id);
        appointment.setStatus(AppointmentStatus.cancelled);
        appointment.setCancelledAt(LocalDateTime.now());

        return MapperUtil.toAppointmentResponse(appointmentRepository.save(appointment));
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita no encontrada: " + id));
    }
}