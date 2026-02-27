package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Permission;
import com.validaya.validaya.entity.Role;
import com.validaya.validaya.repository.PermissionRepository;
import com.validaya.validaya.repository.RoleRepository;
import com.validaya.validaya.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Role create(Role role) {
        if (role.getInstitution() != null && role.getInstitution().getId() != null) {
            if (roleRepository.existsByNameAndInstitutionId(role.getName(), role.getInstitution().getId())) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre en la institución");
            }
        } else {
            if (roleRepository.existsByName(role.getName())) {
                throw new IllegalArgumentException("Ya existe un rol global con ese nombre");
            }
        }
        return roleRepository.save(role);
    }

    @Override
    public Role update(Long id, Role updated) {
        Role existing = findOrThrow(id);
        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new IllegalStateException("Los roles del sistema no son editables");
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return roleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByInstitution(Long institutionId) {
        return roleRepository.findByInstitutionId(institutionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findGlobalRoles() {
        return roleRepository.findByInstitutionIsNull();
    }

    @Override
    public void delete(Long id) {
        Role role = findOrThrow(id);
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new IllegalStateException("Los roles del sistema no pueden eliminarse");
        }
        roleRepository.delete(role);
    }

    @Override
    public void assignPermission(Long roleId, Long permissionId) {
        Role role = findOrThrow(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado: " + permissionId));
        role.getPermissions().add(permission);
        roleRepository.save(role);
        log.info("Permiso {} asignado al rol {}", permission.getName(), role.getName());
    }

    @Override
    public void removePermission(Long roleId, Long permissionId) {
        Role role = findOrThrow(roleId);
        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        roleRepository.save(role);
    }

    private Role findOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
    }
}