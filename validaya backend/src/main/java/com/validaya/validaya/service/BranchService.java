package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.BranchDto;

import java.util.List;
import java.util.Optional;

public interface BranchService {

    /**
     * Crea una nueva sucursal
     * 
     * @param request DTO con datos de la sucursal
     * @return BranchDto.Response con la sucursal creada
     */
    BranchDto.Response create(BranchDto.CreateRequest request);

    /**
     * Actualiza una sucursal existente
     * 
     * @param id Identificador de la sucursal
     * @param request DTO con datos a actualizar
     * @return BranchDto.Response con la sucursal actualizada
     */
    BranchDto.Response update(Long id, BranchDto.CreateRequest request);

    /**
     * Obtiene una sucursal por ID
     * 
     * @param id Identificador de la sucursal
     * @return Optional con BranchDto.Response si existe
     */
    Optional<BranchDto.Response> findById(Long id);

    /**
     * Obtiene sucursales activas de una institución
     * 
     * @param institutionId Identificador de la institución
     * @return Lista de BranchDto.Response
     */
    List<BranchDto.Response> findByInstitution(Long institutionId);

    /**
     * Obtiene sucursales activas por ciudad
     * 
     * @param city Nombre de la ciudad
     * @return Lista de BranchDto.Response
     */
    List<BranchDto.Response> findByCity(String city);

    /**
     * Desactiva una sucursal
     * 
     * @param id Identificador de la sucursal
     */
    void deactivate(Long id);

    /**
     * Activa una sucursal
     * 
     * @param id Identificador de la sucursal
     */
    void activate(Long id);
}
