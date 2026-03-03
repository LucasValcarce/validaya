package com.validaya.validaya.entity;

import com.validaya.validaya.entity.enums.DocumentSource;
import com.validaya.validaya.entity.enums.DocumentStatus;
import com.validaya.validaya.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
        name = "user_documents",
        indexes = {
                @Index(name = "idx_user_doc_type_status",
                        columnList = "user_id, document_type_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    /** Institución que emitió o procesó el documento dentro de la plataforma. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuing_institution_id")
    private Institution issuingInstitution;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    /** Personal que verificó el documento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private InstitutionStaff verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Datos estructurados del documento en JSON.
     * Ej: {"nombre": "Juan Mamani", "ci": "1234567", "departamento": "Santa Cruz"}
     * Cifrar este campo en reposo para documentos con datos sensibles.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_payload", columnDefinition = "jsonb")
    private Map<String, Object> dataPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentSource source;

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
