package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Institution;
import com.validaya.validaya.entity.Procedure;
import com.validaya.validaya.entity.dto.ProcedureDto;
import com.validaya.validaya.repository.InstitutionRepository;
import com.validaya.validaya.repository.ProcedureRepository;
import com.validaya.validaya.service.ProcedureService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcedureServiceImpl implements ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    public ProcedureDto.Response getById(Long id) {
        return MapperUtil.toProcedureResponse(findOrThrow(id));
    }

    @Override
    public ProcedureDto.Response getBySlug(String slug) {
        // "slug" in the API maps to Procedure.code in the entity
        return MapperUtil.toProcedureResponse(procedureRepository.findByCode(slug)
                .orElseThrow(() -> new EntityNotFoundException("Trámite no encontrado: " + slug)));
    }

    @Override
    public List<ProcedureDto.Summary> findAll() {
        return procedureRepository.findByIsActiveTrue().stream()
                .map(MapperUtil::toProcedureSummary).collect(Collectors.toList());
    }

    @Override
    public List<ProcedureDto.Response> findByInstitution(Long institutionId) {
        return procedureRepository.findByInstitutionIdAndIsActiveTrue(institutionId).stream()
                .map(MapperUtil::toProcedureResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProcedureDto.Response create(ProcedureDto.CreateRequest request) {
        Institution inst = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada"));

        String code = normalizeCode(request.getName());

        Procedure procedure = Procedure.builder()
                .institution(inst)
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .estimatedDays(request.getEstimatedDays())
                .basePrice(request.getBasePrice())
                .platformFee(request.getPlatformFee())
                .isActive(true)
                .build();

        return MapperUtil.toProcedureResponse(procedureRepository.save(procedure));
    }

    @Override
    @Transactional
    public ProcedureDto.Response update(Long id, ProcedureDto.CreateRequest request) {
        Procedure procedure = findOrThrow(id);
        if (request.getName() != null) {
            procedure.setName(request.getName());
            // keep code stable unless you explicitly want to change it
        }
        if (request.getDescription() != null) procedure.setDescription(request.getDescription());
        if (request.getBasePrice() != null) procedure.setBasePrice(request.getBasePrice());
        if (request.getPlatformFee() != null) procedure.setPlatformFee(request.getPlatformFee());
        if (request.getEstimatedDays() != null) procedure.setEstimatedDays(request.getEstimatedDays());
        return MapperUtil.toProcedureResponse(procedureRepository.save(procedure));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Procedure p = findOrThrow(id);
        p.setIsActive(false);
        procedureRepository.save(p);
    }

    private Procedure findOrThrow(Long id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trámite no encontrado: " + id));
    }

    private String normalizeCode(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del trámite es requerido");
        }
        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}