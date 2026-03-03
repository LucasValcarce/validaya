package com.validaya.validaya.repository;

import com.validaya.validaya.entity.InstitutionStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionStaffRepository extends JpaRepository<InstitutionStaff, Long> {

    List<InstitutionStaff> findByInstitutionIdAndIsActiveTrue(Long institutionId);

    List<InstitutionStaff> findByUserId(Long userId);

    Optional<InstitutionStaff> findByUserIdAndInstitutionId(Long userId, Long institutionId);

    List<InstitutionStaff> findByBranchIdAndIsActiveTrue(Long branchId);
}
