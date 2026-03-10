package com.validaya.validaya.service;

import com.validaya.validaya.entity.InstitutionStaff;

import java.util.List;
import java.util.Optional;

public interface InstitutionStaffService {

    InstitutionStaff assign(Long userId, Long institutionId, boolean isAdmin, Long branchId);

    InstitutionStaff update(Long staffId, boolean isAdmin, Long branchId);

    Optional<InstitutionStaff> findById(Long id);

    List<InstitutionStaff> findByInstitution(Long institutionId);

    List<InstitutionStaff> findByUser(Long userId);

    Optional<InstitutionStaff> findByUserAndInstitution(Long userId, Long institutionId);

    void deactivate(Long staffId);

    boolean isInstitutionAdmin(Long userId, Long institutionId);
}
