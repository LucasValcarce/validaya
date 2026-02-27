package com.validaya.validaya.service;

import com.validaya.validaya.entity.AuditLog;

import java.util.List;

public interface AuditLogService {

    void log(Long userId, String action, String tableName, Long recordId,
             Object oldValues, Object newValues, String ipAddress);

    List<AuditLog> findByRecord(String tableName, Long recordId);

    List<AuditLog> findByUser(Long userId);
}