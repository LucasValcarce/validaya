package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.InstitutionStaff;
import com.validaya.validaya.entity.UserDocument;
import com.validaya.validaya.entity.enums.DocumentStatus;
import com.validaya.validaya.entity.enums.NotificationChannel;
import com.validaya.validaya.entity.enums.VerificationStatus;
import com.validaya.validaya.repository.InstitutionStaffRepository;
import com.validaya.validaya.repository.UserDocumentRepository;
import com.validaya.validaya.service.AuditService;
import com.validaya.validaya.service.NotificationService;
import com.validaya.validaya.service.UserDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDocumentServiceImpl implements UserDocumentService {

    private final UserDocumentRepository userDocumentRepository;
    private final InstitutionStaffRepository staffRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    public UserDocument register(UserDocument document) {
        if (document.getStatus() == null) {
            document.setStatus(DocumentStatus.pending_verification);
        }
        if (document.getVerificationStatus() == null) {
            document.setVerificationStatus(VerificationStatus.unverified);
        }
        return userDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDocument> findById(Long id) {
        return userDocumentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDocument> findByUser(Long userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDocument> findActiveByUser(Long userId) {
        return userDocumentRepository.findByUserIdAndStatus(userId, DocumentStatus.active);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidDocument(Long userId, Long documentTypeId) {
        // Repository currently doesn’t expose a “valid as of date” query; do a safe in-memory check.
        LocalDate today = LocalDate.now();
        return userDocumentRepository.findByUserIdAndDocumentTypeIdAndStatus(userId, documentTypeId, DocumentStatus.active)
                .filter(d -> d.getVerificationStatus() == VerificationStatus.verified)
                .filter(d -> d.getExpiryDate() == null || !d.getExpiryDate().isBefore(today))
                .isPresent();
    }

    @Override
    public UserDocument verify(Long documentId, Long staffId) {
        UserDocument doc = findOrThrow(documentId);
        InstitutionStaff staff = findStaffOrThrow(staffId);

        doc.setVerificationStatus(VerificationStatus.verified);
        doc.setStatus(DocumentStatus.active);
        doc.setVerifiedBy(staff);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setRejectionReason(null);

        UserDocument saved = userDocumentRepository.save(doc);

        notificationService.send(
                doc.getUser().getId(),
                "Documento verificado",
                "Tu documento fue verificado exitosamente.",
                NotificationChannel.in_app,
                "user_document",
                saved.getId()
        );

        auditService.log(staff.getUser().getId(), "document.verified", "user_documents", saved.getId(),
                null, null, null, null);

        log.info("Documento {} verificado por staff {}", documentId, staffId);
        return saved;
    }

    @Override
    public UserDocument reject(Long documentId, Long staffId, String reason) {
        UserDocument doc = findOrThrow(documentId);
        InstitutionStaff staff = findStaffOrThrow(staffId);

        doc.setVerificationStatus(VerificationStatus.rejected);
        doc.setStatus(DocumentStatus.pending_verification);
        doc.setVerifiedBy(staff);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setRejectionReason(reason);

        UserDocument saved = userDocumentRepository.save(doc);

        notificationService.send(
                doc.getUser().getId(),
                "Documento rechazado",
                reason != null ? reason : "Tu documento fue rechazado.",
                NotificationChannel.in_app,
                "user_document",
                saved.getId()
        );

        auditService.log(staff.getUser().getId(), "document.rejected", "user_documents", saved.getId(),
                null, null, null, null);

        return saved;
    }

    @Override
    public UserDocument revoke(Long documentId, String reason) {
        UserDocument doc = findOrThrow(documentId);
        doc.setStatus(DocumentStatus.revoked);
        doc.setRejectionReason(reason);
        return userDocumentRepository.save(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDocument> findPendingVerificationByInstitution(Long institutionId) {
        // Keep it simple: filter in memory to avoid relying on missing repo methods.
        return userDocumentRepository.findAll().stream()
                .filter(d -> d.getIssuingInstitution() != null)
                .filter(d -> institutionId.equals(d.getIssuingInstitution().getId()))
                .filter(d -> d.getVerificationStatus() == VerificationStatus.unverified)
                .toList();
    }

    @Override
    public void markExpiredDocuments() {
        LocalDate today = LocalDate.now();
        List<UserDocument> toExpire = userDocumentRepository.findAll().stream()
                .filter(d -> d.getStatus() == DocumentStatus.active)
                .filter(d -> d.getExpiryDate() != null && d.getExpiryDate().isBefore(today))
                .toList();

        toExpire.forEach(d -> d.setStatus(DocumentStatus.expired));
        userDocumentRepository.saveAll(toExpire);

        if (!toExpire.isEmpty()) {
            log.info("Documentos marcados como expirados: {}", toExpire.size());
        }
    }

    private UserDocument findOrThrow(Long id) {
        return userDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado: " + id));
    }

    private InstitutionStaff findStaffOrThrow(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff no encontrado: " + staffId));
    }
}
