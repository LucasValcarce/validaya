package com.validaya.validaya.repository;

import com.validaya.validaya.entity.InstitutionStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionStaffRepository extends JpaRepository<InstitutionStaff, Long> {

    List<InstitutionStaff> findByInstitutionIdAndIsActiveTrue(Long institutionId);

    List<InstitutionStaff> findByUserIdAndIsActiveTrue(Long userId);

    Optional<InstitutionStaff> findByUserIdAndInstitutionIdAndIsActiveTrue(Long userId, Long institutionId);

    List<InstitutionStaff> findByBranchIdAndIsActiveTrue(Long branchId);

    /**
     * Comprueba si un usuario tiene un permiso específico en una institución,
     * atravesando la relación role → role_permissions → permissions.
     */
    @Query("""
            SELECT COUNT(s) > 0
            FROM InstitutionStaff s
            JOIN s.role r
            JOIN r.permissions p
            WHERE s.user.id = :userId
              AND s.institution.id = :institutionId
              AND s.isActive = true
              AND p.name = :permissionName
            """)
    boolean hasPermission(@Param("userId") Long userId,
                          @Param("institutionId") Long institutionId,
                          @Param("permissionName") String permissionName);
}
