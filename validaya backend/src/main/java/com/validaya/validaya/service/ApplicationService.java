package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.ApplicationDto;
import com.validaya.validaya.entity.dto.ApplicationDocumentDto;

import java.util.List;

public interface ApplicationService {

    ApplicationDto.Response create(Long userId, ApplicationDto.CreateRequest request);

    ApplicationDto.Response getById(Long id);

    ApplicationDto.Response getByApplicationNumber(String applicationNumber);

    List<ApplicationDto.Summary> findByUser(Long userId);

    List<ApplicationDto.Summary> findByInstitution(Long institutionId);

    ApplicationDto.Response submitDocuments(Long applicationId);

    ApplicationDto.Response review(Long applicationId, ApplicationDto.ReviewRequest request, Long staffId);

    ApplicationDocumentDto.Response addDocument(ApplicationDocumentDto.SubmitRequest request);

    ApplicationDocumentDto.Response reviewDocument(Long docId,
                                                   ApplicationDocumentDto.ReviewRequest request, Long staffId);

    void cancel(Long applicationId, Long userId);
}