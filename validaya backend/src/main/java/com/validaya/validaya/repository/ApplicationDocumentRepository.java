package com.validaya.validaya.repository;

import com.validaya.validaya.entity.ApplicationDocument;
import com.validaya.validaya.entity.enums.AppDocVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findByApplicationId(Long applicationId);

    List<ApplicationDocument> findByApplicationIdAndVerificationStatus(Long applicationId,
                                                                       AppDocVerificationStatus status);

    boolean existsByApplicationIdAndRequirementId(Long applicationId, Long requirementId);
}