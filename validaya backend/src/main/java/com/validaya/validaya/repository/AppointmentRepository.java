package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Appointment;
import com.validaya.validaya.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByTicketId(Long ticketId);

    List<Appointment> findByBranchIdAndAppointmentDate(Long branchId, LocalDate date);

    List<Appointment> findByBranchIdAndStatus(Long branchId, AppointmentStatus status);
}