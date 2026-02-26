package com.validaya.validaya.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clave del permiso. Ej: "docs.verify", "procedure.edit", "application.review"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Módulo funcional al que pertenece. Ej: "documents", "applications", "payments"
     */
    @Column(nullable = false, length = 100)
    private String module;

    @Column(columnDefinition = "TEXT")
    private String description;
}
