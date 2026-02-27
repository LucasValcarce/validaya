package com.validaya.validaya.repository;

import com.validaya.validaya.entity.UserDocument;
import com.validaya.validaya.entity.enums.DocumentStatus;
import com.validaya.validaya.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    List<UserDocument> findByUserId(Long userId);

    List<UserDocument> findByUserIdAndStatus(Long userId, DocumentStatus status);

    Optional<UserDocument> findByUserIdAndDocumentTypeIdAndStatus(
            Long userId, Long documentTypeId, DocumentStatus status);

    List<UserDocument> findByUserIdAndVerificationStatus(Long userId, VerificationStatus verificationStatus);
}