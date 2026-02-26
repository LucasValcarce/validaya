package com.validaya.validaya.entity;

import com.validaya.validaya.entity.enums.ApiAuthType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "api_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "api_name", nullable = false, length = 255)
    private String apiName;

    /** Tipo de documento que valida esta API. NULL = validación genérica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentType documentType;

    @Column(nullable = false, length = 512)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private ApiAuthType authType;

    /**
     * Credenciales encriptadas con AES-256 antes de persistir.
     * Estructura sugerida: {"api_key": "...", "secret": "..."}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> credentials;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;
}
