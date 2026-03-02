package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.*;
import com.validaya.validaya.entity.dto.ApplicationDocumentDto;
import com.validaya.validaya.entity.dto.ApplicationDto;
import com.validaya.validaya.entity.enums.AppDocVerificationStatus;
import com.validaya.validaya.entity.enums.ApplicationStatus;
import com.validaya.validaya.entity.enums.NotificationChannel;
import com.validaya.validaya.repository.*;
import com.validaya.validaya.service.ApplicationService;
import com.validaya.validaya.service.AuditLogService;
import com.validaya.validaya.service.NotificationService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository appDocRepository;
    private final UserRepository userRepository;
    private final ProcedureRepository procedureRepository;
    private final BranchRepository branchRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ApplicationDto.Response create(Long userId, ApplicationDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Procedure procedure = procedureRepository.findById(request.getProcedureId())
                .orElseThrow(() -> new EntityNotFoundException("Trámite no encontrado"));

        // Validate institutionId matches procedure's institution (Application doesn't store institution)
        if (request.getInstitutionId() != null
                && procedure.getInstitution() != null
                && procedure.getInstitution().getId() != null
                && !procedure.getInstitution().getId().equals(request.getInstitutionId())) {
            throw new IllegalArgumentException("El trámite no pertenece a la institución indicada");
        }

        Application app = Application.builder()
                .user(user)
                .procedure(procedure)
                .status(ApplicationStatus.pending_docs)
                .totalAmount(procedure.getBasePrice().add(procedure.getPlatformFee()))
                .build();

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
            app.setBranch(branch);
        }

        app = applicationRepository.save(app);
        log.info("Nueva solicitud creada: id={}", app.getId());

        notificationService.send(
                userId,
                "Solicitud creada",
                "Tu solicitud APP-" + app.getId() + " fue creada exitosamente.",
                NotificationChannel.in_app,
                "application",
                app.getId()
        );

        return MapperUtil.toApplicationResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDto.Response getById(Long id) {
        return MapperUtil.toApplicationResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDto.Response getByApplicationNumber(String applicationNumber) {
        Long id = parseAppNumber(applicationNumber);
        return getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDto.Summary> findByUser(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MapperUtil::toApplicationSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDto.Summary> findByInstitution(Long institutionId) {
        // Application doesn't store institution_id: filter by procedure.institution
        return applicationRepository.findAll().stream()
                .filter(a -> a.getProcedure() != null && a.getProcedure().getInstitution() != null)
                .filter(a -> institutionId.equals(a.getProcedure().getInstitution().getId()))
                .map(MapperUtil::toApplicationSummary)
                .toList();
    }

    @Override
    @Transactional
    public ApplicationDto.Response submitDocuments(Long applicationId) {
        Application app = findOrThrow(applicationId);
        assertStatus(app, ApplicationStatus.pending_docs);

        // TODO hacer la busceda y subida de documentos
        app.setStatus(ApplicationStatus.docs_submitted);
        // No submittedAt field in entity
        return MapperUtil.toApplicationResponse(applicationRepository.save(app));
    }

    @Override
    @Transactional
    public ApplicationDto.Response review(Long applicationId, ApplicationDto.ReviewRequest request, Long staffId) {
        Application app = findOrThrow(applicationId);
        ApplicationStatus old = app.getStatus();

        app.setStatus(request.getNewStatus());
        if (request.getRejectionReason() != null) {
            app.setRejectionReason(request.getRejectionReason());
        }
        if (request.getNewStatus() == ApplicationStatus.completed) {
            app.setCompletedAt(LocalDateTime.now());
        }

        app = applicationRepository.save(app);
        auditLogService.log(staffId, "application.review", "applications", app.getId(), old, request.getNewStatus(), null);
        return MapperUtil.toApplicationResponse(app);
    }

    @Override
    @Transactional
    public ApplicationDocumentDto.Response addDocument(ApplicationDocumentDto.SubmitRequest request) {
        Application app = findOrThrow(request.getApplicationId());

        UserDocument userDoc = userDocumentRepository.findById(request.getUserDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado"));

        if (request.getRequirementId() == null) {
            throw new IllegalArgumentException("requirementId es requerido");
        }

        ProcedureDocumentRequirement req = ProcedureDocumentRequirement.builder()
                .id(request.getRequirementId())
                .build();

        ApplicationDocument appDoc = ApplicationDocument.builder()
                .application(app)
                .userDocument(userDoc)
                .requirement(req)
                .verificationStatus(AppDocVerificationStatus.pending)
                .build();

        return MapperUtil.toAppDocResponse(appDocRepository.save(appDoc));
    }

    @Override
    @Transactional
    public ApplicationDocumentDto.Response reviewDocument(Long docId,
                                                          ApplicationDocumentDto.ReviewRequest request,
                                                          Long staffId) {
        ApplicationDocument doc = appDocRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Documento de solicitud no encontrado"));

        doc.setVerificationStatus(request.getVerificationStatus());
        doc.setVerifiedAt(LocalDateTime.now());

        if (request.getRejectionReason() != null) {
            doc.setComments(request.getRejectionReason());
        }

        return MapperUtil.toAppDocResponse(appDocRepository.save(doc));
    }

    @Override
    @Transactional
    public void cancel(Long applicationId, Long userId) {
        Application app = findOrThrow(applicationId);
        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalStateException("No autorizado para cancelar esta solicitud");
        }
        app.setStatus(ApplicationStatus.cancelled);
        applicationRepository.save(app);
    }

    private Application findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));
    }

    private void assertStatus(Application app, ApplicationStatus expected) {
        if (app.getStatus() != expected) {
            throw new IllegalStateException(
                    "Estado incorrecto. Esperado: " + expected + ", actual: " + app.getStatus());
        }
    }

    private Long parseAppNumber(String applicationNumber) {
        if (applicationNumber == null || applicationNumber.isBlank()) {
            throw new IllegalArgumentException("applicationNumber inválido");
        }
        String normalized = applicationNumber.trim().toUpperCase();
        if (!normalized.startsWith("APP-")) {
            throw new IllegalArgumentException("applicationNumber inválido: " + applicationNumber);
        }
        try {
            return Long.parseLong(normalized.substring(4));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("applicationNumber inválido: " + applicationNumber);
        }
    }
}