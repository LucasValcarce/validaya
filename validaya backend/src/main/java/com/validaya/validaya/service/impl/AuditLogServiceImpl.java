package com.validaya.validaya.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.validaya.validaya.entity.AuditLog;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.repository.AuditLogRepository;
import com.validaya.validaya.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String tableName, Long recordId,
                    Object oldValues, Object newValues, String ipAddress) {
        try {
            User user = (userId != null) ? User.builder().id(userId).build() : null;

            Map<String, Object> oldData = toMap(oldValues);
            Map<String, Object> newData = toMap(newValues);

            AuditLog entry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .tableName(tableName)
                    .recordId(recordId)
                    .oldData(oldData)
                    .newData(newData)
                    .ipAddress(ipAddress)
                    .userAgent(null)
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Error guardando audit log para acción: {} en {}", action, tableName, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByRecord(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) return null;
        if (value instanceof Map<?, ?> m) {
            //noinspection unchecked
            return (Map<String, Object>) m;
        }
        return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {});
    }
}