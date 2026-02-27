package com.validaya.validaya.service;

import com.validaya.validaya.entity.UserDocument;

import java.util.List;
import java.util.Optional;

public interface UserDocumentService {

    /**
     * Registra un documento generado por la plataforma para un usuario.
     * Cifra el data_payload si contiene información sensible.
     */
    UserDocument register(UserDocument document);

    Optional<UserDocument> findById(Long id);

    List<UserDocument> findByUser(Long userId);

    List<UserDocument> findActiveByUser(Long userId);

    /**
     * Verifica si el usuario tiene el tipo de documento activo y vigente.
     */
    boolean hasValidDocument(Long userId, Long documentTypeId);

    /**
     * Verifica un documento como válido. Solo personal de institución puede hacer esto.
     *
     * @param documentId ID del documento
     * @param staffId    ID del miembro del staff que verifica
     */
    UserDocument verify(Long documentId, Long staffId);

    /**
     * Rechaza un documento con un motivo.
     */
    UserDocument reject(Long documentId, Long staffId, String reason);

    /**
     * Revoca un documento (pérdida, robo, reemplazo).
     */
    UserDocument revoke(Long documentId, String reason);

    List<UserDocument> findPendingVerificationByInstitution(Long institutionId);

    void markExpiredDocuments();
}
