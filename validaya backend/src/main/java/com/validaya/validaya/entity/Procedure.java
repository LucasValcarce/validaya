package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procedures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Tarifa cobrada por la plataforma además del precio base de la institución. */
    @Column(name = "platform_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal platformFee = BigDecimal.ZERO;

    /** Tipo de documento que genera este trámite al completarse. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "output_document_type_id")
    private DocumentType outputDocumentType;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "requires_appointment")
    @Builder.Default
    private Boolean requiresAppointment = true;

    @Column(name = "appointment_duration_min")
    @Builder.Default
    private Integer appointmentDurationMin = 15;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
