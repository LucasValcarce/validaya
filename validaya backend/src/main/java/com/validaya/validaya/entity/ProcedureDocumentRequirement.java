package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "procedure_document_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    /**
     * Antigüedad máxima aceptable del documento en meses.
     * NULL = sin restricción de antigüedad.
     */
    @Column(name = "max_age_months")
    private Integer maxAgeMonths;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
