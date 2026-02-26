package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    /** Código corto único. Ej: "CI", "CERT_NAC", "CERT_DOM", "TITULO_UNIV" */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Institución que emite este tipo de documento. NULL = emitido por cualquier institución. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuing_institution_id")
    private Institution issuingInstitution;

    /** Meses de vigencia estándar. NULL = indefinido. */
    @Column(name = "validity_months")
    private Integer validityMonths;

    @Column(name = "requires_renewal")
    @Builder.Default
    private Boolean requiresRenewal = false;

    @Column(name = "is_biometric")
    @Builder.Default
    private Boolean isBiometric = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
