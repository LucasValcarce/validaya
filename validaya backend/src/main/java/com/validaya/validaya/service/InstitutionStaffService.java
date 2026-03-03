package com.validaya.validaya.service;

import com.validaya.validaya.entity.InstitutionStaff;

import java.util.List;
import java.util.Optional;

public interface InstitutionStaffService {

    /**
     * Asigna un ciudadano como staff o admin de una institución.
     * Cambia su user_type a 'staff' o 'institution_admin'.
     * @param userId ID del usuario (debe ser citizen)
     * @param institutionId ID de la institución
     * @param isAdmin true para institution_admin, false para staff. El user_type se actualiza en User.
     * @param branchId ID de la sucursal (opcional)
     */
    InstitutionStaff assign(Long userId, Long institutionId, boolean isAdmin, Long branchId);

    /**
     * Actualiza sucursal y tipo de staff.
     * El rol se cambia a través de User.userType.
     */
    InstitutionStaff update(Long staffId, boolean isAdmin, Long branchId);

    Optional<InstitutionStaff> findById(Long id);

    List<InstitutionStaff> findByInstitution(Long institutionId);

    List<InstitutionStaff> findByUser(Long userId);

    Optional<InstitutionStaff> findByUserAndInstitution(Long userId, Long institutionId);

    /**
     * Desactiva el staff y cambia su user_type de vuelta a 'citizen'.
     */
    void deactivate(Long staffId);

    /**
     * Verifica si un usuario tiene rol de admin en una institución específica.
     */
    boolean isInstitutionAdmin(Long userId, Long institutionId);
}
