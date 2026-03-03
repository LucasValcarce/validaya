package com.validaya.validaya.controller;

import com.validaya.validaya.entity.InstitutionStaff;
import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.InstitutionStaffDto;
import com.validaya.validaya.service.InstitutionStaffService;
import com.validaya.validaya.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/institution-staff")
@RequiredArgsConstructor
public class InstitutionStaffController {

    private final InstitutionStaffService institutionStaffService;

    /**
     * Asigna un ciudadano como staff o admin de una institución.
     * Solo institution_admin o admin pueden hacer esto.
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ROLE_institution_admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<InstitutionStaffDto.Response>> assign(
            @RequestBody InstitutionStaffDto.AssignRequest request) {
        InstitutionStaff staff = institutionStaffService.assign(
                request.getUserId(),
                request.getInstitutionId(),
                request.isAdmin(),
                request.getBranchId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Staff asignado correctamente", toResponse(staff)));
    }

    /**
     * Actualiza tipo de staff (admin/staff) y sucursal.
     * Solo institution_admin o admin pueden hacer esto.
     */
    @PutMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('ROLE_institution_admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<InstitutionStaffDto.Response>> update(
            @PathVariable Long staffId,
            @RequestBody InstitutionStaffDto.UpdateRequest request) {
        InstitutionStaff staff = institutionStaffService.update(
                staffId,
                request.isAdmin(),
                request.getBranchId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Staff actualizado", toResponse(staff)));
    }

    /**
     * Lista el staff de una institución.
     */
    @GetMapping("/institution/{institutionId}")
    @PreAuthorize("hasAnyRole('ROLE_institution_admin', 'ROLE_staff', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<List<InstitutionStaffDto.Response>>> listByInstitution(
            @PathVariable Long institutionId) {
        List<InstitutionStaff> staffList = institutionStaffService.findByInstitution(institutionId);
        List<InstitutionStaffDto.Response> responses = staffList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    /**
     * Obtiene los detalles de un staff específico.
     */
    @GetMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('ROLE_institution_admin', 'ROLE_staff', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<InstitutionStaffDto.Response>> getById(
            @PathVariable Long staffId) {
        InstitutionStaff staff = institutionStaffService.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff no encontrado"));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(staff)));
    }

    /**
     * Desactiva un staff y lo convierte de vuelta a ciudadano.
     * Solo institution_admin o admin pueden hacer esto.
     */
    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('ROLE_institution_admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long staffId) {
        institutionStaffService.deactivate(staffId);
        return ResponseEntity.ok(ApiResponse.ok("Staff desactivado correctamente", null));
    }

    private InstitutionStaffDto.Response toResponse(InstitutionStaff staff) {
        return MapperUtil.toInstitutionStaffResponse(staff);
    }
}
