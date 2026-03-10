package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.ProcedureDto;
import com.validaya.validaya.service.ProcedureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procedures")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureService procedureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProcedureDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcedureDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.getById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProcedureDto.Response>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.getBySlug(slug)));
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<ApiResponse<List<ProcedureDto.Response>>> findByInstitution(
            @PathVariable Long institutionId) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.findByInstitution(institutionId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ProcedureDto.Response>> create(
            @Valid @RequestBody ProcedureDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<ProcedureDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcedureDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(procedureService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        procedureService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Trámite desactivado", null));
    }
}