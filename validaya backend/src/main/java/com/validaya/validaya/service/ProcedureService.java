package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.ProcedureDto;

import java.util.List;

public interface ProcedureService {

    ProcedureDto.Response getById(Long id);

    ProcedureDto.Response getBySlug(String slug);

    List<ProcedureDto.Summary> findAll();

    List<ProcedureDto.Response> findByInstitution(Long institutionId);

    ProcedureDto.Response create(ProcedureDto.CreateRequest request);

    ProcedureDto.Response update(Long id, ProcedureDto.CreateRequest request);

    void deactivate(Long id);
}