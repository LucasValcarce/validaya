package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.InstitutionDto;

import java.util.List;

public interface InstitutionService {

    InstitutionDto.Response getById(Long id);

    InstitutionDto.Response getBySlug(String slug);

    List<InstitutionDto.Response> findAll();

    List<InstitutionDto.Summary> findAllSummaries();

    InstitutionDto.Response create(InstitutionDto.CreateRequest request);

    InstitutionDto.Response update(Long id, InstitutionDto.CreateRequest request);

    void deactivate(Long id);
}