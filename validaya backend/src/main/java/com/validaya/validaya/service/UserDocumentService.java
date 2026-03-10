package com.validaya.validaya.service;

import com.validaya.validaya.entity.UserDocument;

import java.util.List;
import java.util.Optional;

public interface UserDocumentService {

    UserDocument register(UserDocument document);

    Optional<UserDocument> findById(Long id);

    List<UserDocument> findByUser(Long userId);

    List<UserDocument> findActiveByUser(Long userId);

    boolean hasValidDocument(Long userId, Long documentTypeId);

    UserDocument verify(Long documentId, Long staffId);

    UserDocument reject(Long documentId, Long staffId, String reason);

    UserDocument revoke(Long documentId, String reason);

    List<UserDocument> findPendingVerificationByInstitution(Long institutionId);

    void markExpiredDocuments();
}
