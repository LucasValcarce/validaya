package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.ApplicationDocumentDto;
import com.validaya.validaya.entity.dto.ApplicationDto;
import com.validaya.validaya.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> create(
            @RequestParam Long userId,
            @Valid @RequestBody ApplicationDto.CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.create(userId, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getById(id)));
    }

    @GetMapping("/number/{applicationNumber}")
    public ResponseEntity<ApiResponse<ApplicationDto.Response>> getByNumber(
            @PathVariable String applicationNumber) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.getByApplicationNumber(applicationNumber)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ApplicationDto.Summary>>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.findByUser(userId)));
    }

    @GetMapping("/institution/{institutionId}")
    @PreAuthorize("hasAnyRole('admin','staff', 'institution_admin')")
    public ResponseEntity<ApiResponse<List<ApplicationDto.Summary>>> findByInstitution(
            @PathVariable Long institutionId) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.findByInstitution(institutionId)));
    }
}