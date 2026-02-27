package com.validaya.validaya.service;

import com.validaya.validaya.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {

    /**
     * Registra una acción de auditoría.
     *
     * @param userId    ID del usuario que realizó la acción (null = sistema)
     * @param action    clave de acción (ej. "document.verified")
     * @param tableName tabla afectada
     * @param recordId  ID del registro afectado
     * @param oldData   estado anterior (puede ser null para creaciones)
     * @param newData   estado nuevo (puede ser null para eliminaciones)
     * @param ipAddress IP del cliente
     */
    AuditLog log(Long userId, String action, String tableName, Long recordId,
                 Map<String, Object> oldData, Map<String, Object> newData,
                 String ipAddress, String userAgent);

    List<AuditLog> findByUser(Long userId);

    List<AuditLog> findByRecord(String tableName, Long recordId);

    List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to);
}
