package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    List<Procedure> findByInstitutionIdAndIsActiveTrue(Long institutionId);

    List<Procedure> findByIsActiveTrue();

    Optional<Procedure> findByCode(String code);

    @Query("SELECT p FROM Procedure p WHERE p.institution.id = :institutionId AND p.isActive = true ORDER BY p.name")
    List<Procedure> findActiveByInstitution(Long institutionId);
}