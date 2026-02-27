package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.AuditLog;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.repository.AuditLogRepository;
import com.validaya.validaya.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * El audit log se escribe en una transacción independiente para que no se pierda
     * si la transacción principal hace rollback.
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(Long userId, String action, String tableName, Long recordId,
                        Map<String, Object> oldData, Map<String, Object> newData,
                        String ipAddress, String userAgent) {
        User user = userId != null ? User.builder().id(userId).build() : null;

        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .tableName(tableName)
                .recordId(recordId)
                .oldData(oldData)
                .newData(newData)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByRecord(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByCreatedAtBetween(from, to);
    }
}
