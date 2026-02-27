package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    // Added to match RoleServiceImpl expectations
    boolean existsByNameAndInstitutionId(String name, Long institutionId);

    List<Role> findByInstitutionId(Long institutionId);

    List<Role> findByInstitutionIsNull();
}