package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "institution_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "employee_code", length = 50)
    private String employeeCode;

    /**
     * Rol dentro de la institución se determina por User.userType:
     * - institution_admin: administrador de institución
     * - staff: personal de la institución
     * Los permisos se manejan completamente a través de UserType.
     */

    /** Sucursal asignada. NULL = puede atender cualquier sucursal de la institución. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    /** Si el acceso es temporal se puede establecer una fecha de expiración. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}
