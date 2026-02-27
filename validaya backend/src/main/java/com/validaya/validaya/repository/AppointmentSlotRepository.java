package com.validaya.validaya.repository;

import com.validaya.validaya.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByBranchIdAndSlotDateBetweenAndIsBlockedFalse(
            Long branchId, LocalDate startDate, LocalDate endDate);

    Optional<AppointmentSlot> findByBranchIdAndSlotDateAndSlotTime(
            Long branchId, LocalDate slotDate, LocalTime slotTime);

    @Query("""
            SELECT s
            FROM AppointmentSlot s
            WHERE s.branch.id = :branchId
              AND s.slotDate = :slotDate
              AND s.isBlocked = false
              AND s.reservedCount < s.maxCapacity
            """)
    List<AppointmentSlot> findAvailableSlots(Long branchId, LocalDate slotDate);
}