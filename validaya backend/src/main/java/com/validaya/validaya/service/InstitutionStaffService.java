package com.validaya.validaya.service;

import com.validaya.validaya.entity.InstitutionStaff;

import java.util.List;
import java.util.Optional;

public interface InstitutionStaffService {

    InstitutionStaff assign(Long userId, Long institutionId, Long roleId, Long branchId);

    InstitutionStaff update(Long staffId, Long roleId, Long branchId);

    Optional<InstitutionStaff> findById(Long id);

    List<InstitutionStaff> findByInstitution(Long institutionId);

    List<InstitutionStaff> findByUser(Long userId);

    Optional<InstitutionStaff> findByUserAndInstitution(Long userId, Long institutionId);

    void deactivate(Long staffId);

    boolean hasPermission(Long userId, Long institutionId, String permissionName);
}
