package com.validaya.validaya.entity;

import com.validaya.validaya.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 50)
    private String identification;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    /**
     * Vector de embeddings faciales (128 o 512 floats).
     * Almacenado como JSON. Con PostgreSQL + pgvector se puede cambiar a VECTOR.
     * Debe cifrarse en reposo (AES-256) a nivel de aplicación antes de persistir.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "face_vector", columnDefinition = "jsonb")
    private List<Double> faceVector;

    @Column(name = "face_registered_at")
    private LocalDateTime faceRegisteredAt;

    @Column(name = "face_verified")
    @Builder.Default
    private Boolean faceVerified = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

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
