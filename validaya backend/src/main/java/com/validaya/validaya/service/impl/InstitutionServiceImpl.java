package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Institution;
import com.validaya.validaya.entity.dto.InstitutionDto;
import com.validaya.validaya.repository.InstitutionRepository;
import com.validaya.validaya.service.InstitutionService;
import com.validaya.validaya.utils.MapperUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;

    @Override
    public InstitutionDto.Response getById(Long id) {
        return MapperUtil.toInstitutionResponse(findOrThrow(id));
    }

    @Override
    public InstitutionDto.Response getBySlug(String slug) {
        Institution inst = institutionRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + slug));
        return MapperUtil.toInstitutionResponse(inst);
    }

    @Override
    public List<InstitutionDto.Response> findAll() {
        return institutionRepository.findByIsActiveTrue().stream()
                .map(MapperUtil::toInstitutionResponse).collect(Collectors.toList());
    }

    @Override
    public List<InstitutionDto.Summary> findAllSummaries() {
        return institutionRepository.findByIsActiveTrue().stream()
                .map(MapperUtil::toInstitutionSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InstitutionDto.Response create(InstitutionDto.CreateRequest request) {
        if (institutionRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("El slug ya está en uso: " + request.getSlug());
        }

        Institution inst = Institution.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .institutionType(request.getType())
                .description(request.getDescription())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .isActive(true)
                .build();

        return MapperUtil.toInstitutionResponse(institutionRepository.save(inst));
    }

    @Override
    @Transactional
    public InstitutionDto.Response update(Long id, InstitutionDto.CreateRequest request) {
        Institution inst = findOrThrow(id);
        if (request.getName() != null) inst.setName(request.getName());
        if (request.getDescription() != null) inst.setDescription(request.getDescription());
        if (request.getContactEmail() != null) inst.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) inst.setContactPhone(request.getContactPhone());
        if (request.getType() != null) inst.setInstitutionType(request.getType());
        return MapperUtil.toInstitutionResponse(institutionRepository.save(inst));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Institution inst = findOrThrow(id);
        inst.setIsActive(false);
        institutionRepository.save(inst);
    }

    private Institution findOrThrow(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Institución no encontrada: " + id));
    }
}