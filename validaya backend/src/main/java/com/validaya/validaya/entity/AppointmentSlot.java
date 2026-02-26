package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointment_slots",
        indexes = {
                @Index(name = "idx_slot_branch_date_time",
                        columnList = "branch_id, slot_date, slot_time")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_slot_branch_date_time",
                        columnNames = {"branch_id", "slot_date", "slot_time"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    /** Se incrementa/decrementa con cada reserva o cancelación. */
    @Column(name = "reserved_count")
    @Builder.Default
    private Integer reservedCount = 0;

    /** Bloqueado manualmente por feriado, cierre técnico, etc. */
    @Column(name = "is_blocked")
    @Builder.Default
    private Boolean isBlocked = false;
}
