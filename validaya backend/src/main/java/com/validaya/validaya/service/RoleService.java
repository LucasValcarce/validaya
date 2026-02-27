package com.validaya.validaya.service;

import com.validaya.validaya.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Role create(Role role);

    Role update(Long id, Role updated);

    Optional<Role> findById(Long id);

    List<Role> findByInstitution(Long institutionId);

    /** Roles globales del sistema (no pertenecen a ninguna institución). */
    List<Role> findGlobalRoles();

    void delete(Long id);

    void assignPermission(Long roleId, Long permissionId);

    void removePermission(Long roleId, Long permissionId);
}
