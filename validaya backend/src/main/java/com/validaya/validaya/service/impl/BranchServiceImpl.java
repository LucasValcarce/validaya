package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Branch;
import com.validaya.validaya.entity.Institution;
import com.validaya.validaya.entity.dto.BranchDto;
import com.validaya.validaya.repository.BranchRepository;
import com.validaya.validaya.repository.InstitutionRepository;
import com.validaya.validaya.service.BranchService;
import com.validaya.validaya.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final InstitutionRepository institutionRepository;

    /**
     * Crea una nueva sucursal
     * 
     * @param request DTO con datos de la sucursal
     * @return BranchDto.Response con la sucursal creada
     */
    @Override
    @Transactional
    public BranchDto.Response create(BranchDto.CreateRequest request) {
        // Validar que la institución existe
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> {
                    log.warn("Intento de crear sucursal con institución inexistente: {}", 
                            request.getInstitutionId());
                    return new IllegalArgumentException("Institución no encontrada");
                });

        // Mapear DTO a entidad
        Branch branch = Branch.builder()
                .institution(institution)
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .lat(request.getLatitude())
                .lng(request.getLongitude())
                .schedule(request.getSchedule())
                .maxDailyAppointments(request.getMaxDailyAppointments())
                .isActive(true)
                .build();

        branch = branchRepository.save(branch);
        log.info("Sucursal creada: id={}, nombre={}, institución={}", 
                branch.getId(), branch.getName(), institution.getName());

        return MapperUtil.toBranchResponse(branch);
    }

    /**
     * Actualiza una sucursal existente
     * 
     * @param id Identificador de la sucursal
     * @param request DTO con datos a actualizar
     * @return BranchDto.Response con la sucursal actualizada
     */
    @Override
    @Transactional
    public BranchDto.Response update(Long id, BranchDto.CreateRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de actualizar sucursal inexistente: {}", id);
                    return new IllegalArgumentException("Sucursal no encontrada");
                });

        // Validar institución si cambia
        if (!branch.getInstitution().getId().equals(request.getInstitutionId())) {
            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> {
                        log.warn("Intento de actualizar sucursal a institución inexistente: {}", 
                                request.getInstitutionId());
                        return new IllegalArgumentException("Institución no encontrada");
                    });
            branch.setInstitution(institution);
        }

        // Actualizar campos
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setLat(request.getLatitude());
        branch.setLng(request.getLongitude());
        branch.setSchedule(request.getSchedule());
        branch.setMaxDailyAppointments(request.getMaxDailyAppointments());

        branch = branchRepository.save(branch);
        log.info("Sucursal actualizada: id={}, nombre={}", branch.getId(), branch.getName());

        return MapperUtil.toBranchResponse(branch);
    }

    /**
     * Obtiene una sucursal por ID
     * 
     * @param id Identificador de la sucursal
     * @return Optional con BranchDto.Response si existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BranchDto.Response> findById(Long id) {
        return branchRepository.findById(id)
                .map(MapperUtil::toBranchResponse);
    }

    /**
     * Obtiene sucursales activas de una institución
     * 
     * @param institutionId Identificador de la institución
     * @return Lista de BranchDto.Response
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchDto.Response> findByInstitution(Long institutionId) {
        return branchRepository.findByInstitutionIdAndIsActiveTrue(institutionId)
                .stream()
                .map(MapperUtil::toBranchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene sucursales activas por ciudad
     * 
     * @param city Nombre de la ciudad
     * @return Lista de BranchDto.Response
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchDto.Response> findByCity(String city) {
        return branchRepository.findByCity(city)
                .stream()
                .map(MapperUtil::toBranchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Desactiva una sucursal
     * 
     * @param id Identificador de la sucursal
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de desactivar sucursal inexistente: {}", id);
                    return new IllegalArgumentException("Sucursal no encontrada");
                });

        branch.setIsActive(false);
        branchRepository.save(branch);
        log.info("Sucursal desactivada: id={}", id);
    }

    /**
     * Activa una sucursal
     * 
     * @param id Identificador de la sucursal
     */
    @Override
    @Transactional
    public void activate(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de activar sucursal inexistente: {}", id);
                    throw new IllegalArgumentException("Sucursal no encontrada");
                });

        branch.setIsActive(true);
        branchRepository.save(branch);
        log.info("Sucursal activada: id={}", id);
    }
}
