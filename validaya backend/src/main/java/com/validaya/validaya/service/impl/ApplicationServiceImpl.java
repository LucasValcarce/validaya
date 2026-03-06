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
import java.util.ArrayList;
import java.util.Map;

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
    private final ProcedureDocumentRequirementRepository documentRequirementRepository;
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
                .status(ApplicationStatus.pending_documents)
                .totalAmount(procedure.getBasePrice().add(procedure.getPlatformFee()))
                .build();

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
            app.setBranch(branch);
        }

        app = applicationRepository.save(app);
        log.info("Nueva solicitud creada: id={}", app.getId());

        // Validación automática de documentos
        ApplicationDto.Response response = MapperUtil.toApplicationResponse(app);
        ApplicationDto.DocumentValidationResponse validation = performAutomaticDocumentValidation(app);
        
        // Mapear información de validación a la respuesta
        response.setDocumentValidationCompleted(true);
        response.setAllDocumentsPresent(validation.isAllDocumentsPresent());
        response.setMissingDocuments(validation.getMissingDocuments());
        
        // Si tiene todos los documentos, cambiar estado a payment_pending automáticamente
        if (validation.isAllDocumentsPresent()) {
            app.setStatus(ApplicationStatus.payment_pending);
            applicationRepository.save(app);
            response.setStatus(ApplicationStatus.payment_pending);
            
            log.info("Solicitud {} automáticamente verificada. Estado: payment_pending", app.getId());
            notificationService.send(
                    userId,
                    "Solicitud verificada",
                    "Tu solicitud APP-" + app.getId() + " ha sido verificada exitosamente. Procede al pago.",
                    NotificationChannel.in_app,
                    "application",
                    app.getId()
            );
        } else {
            log.info("Solicitud {} requiere documentos adicionales", app.getId());
            notificationService.send(
                    userId,
                    "Documentos faltantes",
                    "Tu solicitud APP-" + app.getId() + " requiere " + validation.getMissingDocuments().size() + " documentos adicionales.",
                    NotificationChannel.in_app,
                    "application",
                    app.getId()
            );
        }

        return response;
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


    private ApplicationDto.DocumentValidationResponse performAutomaticDocumentValidation(Application app) {
        Procedure procedure = app.getProcedure();
        User user = app.getUser();

        // Obtener requisitos del procedimiento
        List<ProcedureDocumentRequirement> requirements = 
                documentRequirementRepository.findByProcedureIdOrderByDisplayOrder(procedure.getId());

        // Obtener documentos del usuario
        List<UserDocument> userDocuments = userDocumentRepository.findByUserId(user.getId());
        Map<Long, UserDocument> userDocMap = userDocuments.stream()
                .collect(Collectors.toMap(doc -> doc.getDocumentType().getId(), doc -> doc));

        // Construir respuesta de validación
        ApplicationDto.DocumentValidationResponse response = new ApplicationDto.DocumentValidationResponse();
        response.setApplicationId(app.getId());
        response.setApplicationNumber("APP-" + app.getId());
        response.setStatus(app.getStatus());
        response.setRequiredDocuments(new ArrayList<>());
        response.setMissingDocuments(new ArrayList<>());

        // Comparar requisitos con documentos del usuario
        for (ProcedureDocumentRequirement req : requirements) {
            ApplicationDto.DocumentValidationResponse.RequiredDocumentDto docDto = 
                    new ApplicationDto.DocumentValidationResponse.RequiredDocumentDto();
            
            docDto.setDocumentTypeId(req.getDocumentType().getId());
            docDto.setDocumentTypeName(req.getDocumentType().getName());
            docDto.setMandatory(req.getIsMandatory() != null ? req.getIsMandatory() : true);
            docDto.setMaxAgeMonths(req.getMaxAgeMonths());
            docDto.setNotes(req.getNotes());

            UserDocument userDoc = userDocMap.get(req.getDocumentType().getId());
            if (userDoc != null) {
                docDto.setPresent(true);
                docDto.setUserDocumentCreatedAt(userDoc.getCreatedAt());
                response.getRequiredDocuments().add(docDto);
            } else {
                docDto.setPresent(false);
                if (req.getIsMandatory() != null && req.getIsMandatory()) {
                    response.getMissingDocuments().add(docDto);
                }
                response.getRequiredDocuments().add(docDto);
            }
        }

        // Determinar si tiene todos los documentos requeridos
        boolean allDocumentsPresent = response.getMissingDocuments().isEmpty();
        response.setAllDocumentsPresent(allDocumentsPresent);

        return response;
    }

    private Application findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));
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