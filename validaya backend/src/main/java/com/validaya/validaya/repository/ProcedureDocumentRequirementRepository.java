package com.validaya.validaya.repository;

import com.validaya.validaya.entity.ProcedureDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureDocumentRequirementRepository extends JpaRepository<ProcedureDocumentRequirement, Long> {

    List<ProcedureDocumentRequirement> findByProcedureIdOrderByDisplayOrder(Long procedureId);

    List<ProcedureDocumentRequirement> findByProcedureIdAndIsMandatoryTrue(Long procedureId);

    boolean existsByProcedureIdAndDocumentTypeId(Long procedureId, Long documentTypeId);

    void deleteByProcedureId(Long procedureId);

    // ProcedureDocumentRequirementRepository
    Optional<ProcedureDocumentRequirement> findByProcedureIdAndDocumentTypeId(Long procedureId, Long documentTypeId);
}
