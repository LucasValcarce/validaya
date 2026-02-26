package com.validaya.validaya.entity;

import com.validaya.validaya.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    /** Código alfanumérico único visible al ciudadano. Ej: "TK-8847" */
    @Column(name = "ticket_code", nullable = false, unique = true, length = 20)
    private String ticketCode;

    /**
     * Contenido del QR. Puede incluir el código del ticket + hash firmado
     * para evitar falsificaciones.
     */
    @Column(name = "qr_payload", columnDefinition = "TEXT")
    private String qrPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt;

    /** Fecha/hora límite para presentar el ticket. Se actualiza al agendar la cita. */
    @Column(name = "expiry_at", nullable = false)
    private LocalDateTime expiryAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** Empleado que validó y procesó el ticket en la sucursal. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_staff_id")
    private InstitutionStaff usedByStaff;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }
}
