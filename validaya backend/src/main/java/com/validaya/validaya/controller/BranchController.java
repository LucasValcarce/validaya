package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.BranchDto;
import com.validaya.validaya.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDto.Response>> getById(@PathVariable Long id) {
        return branchService.findById(id)
                .map(branch -> ResponseEntity.ok(ApiResponse.ok(branch)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sucursal no encontrada")));
    }

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<ApiResponse<List<BranchDto.Response>>> getByInstitution(
            @PathVariable Long institutionId) {
        List<BranchDto.Response> branches = branchService.findByInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<BranchDto.Response>>> getByCity(
            @PathVariable String city) {
        List<BranchDto.Response> branches = branchService.findByCity(city);
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<BranchDto.Response>> create(
            @Valid @RequestBody BranchDto.CreateRequest request) {
        BranchDto.Response branch = branchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Sucursal creada correctamente", branch));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<BranchDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchDto.CreateRequest request) {
        try {
            BranchDto.Response branch = branchService.update(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Sucursal actualizada correctamente", branch));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        try {
            branchService.deactivate(id);
            return ResponseEntity.ok(ApiResponse.ok("Sucursal desactivada correctamente", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        try {
            branchService.activate(id);
            return ResponseEntity.ok(ApiResponse.ok("Sucursal activada correctamente", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
