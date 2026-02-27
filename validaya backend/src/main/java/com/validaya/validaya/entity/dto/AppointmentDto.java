package com.validaya.validaya.entity.dto;

import com.validaya.validaya.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentDto {

    @Data
    public static class ScheduleRequest {
        @NotNull
        private Long ticketId;
        @NotNull
        private Long branchId;
        @NotNull
        private Long slotId;
    }

    @Data
    public static class Response {
        private Long id;
        private Long ticketId;
        private String ticketCode;
        private Long branchId;
        private String branchName;
        private LocalDate appointmentDate;
        private LocalTime appointmentTime;
        private AppointmentStatus status;
        private String staffNotes;
        private LocalDateTime confirmedAt;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SlotResponse {
        private Long id;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer capacity;
        private Integer booked;
        private boolean available;
    }

    @Data
    public static class AvailabilityRequest {
        @NotNull
        private Long branchId;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
    }
}