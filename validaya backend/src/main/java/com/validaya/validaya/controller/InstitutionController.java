package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.InstitutionDto;
import com.validaya.validaya.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstitutionDto.Summary>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.findAllSummaries()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstitutionDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.getById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<InstitutionDto.Response>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.getBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<InstitutionDto.Response>> create(
            @Valid @RequestBody InstitutionDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<InstitutionDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody InstitutionDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(institutionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        institutionService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Institución desactivada", null));
    }
}