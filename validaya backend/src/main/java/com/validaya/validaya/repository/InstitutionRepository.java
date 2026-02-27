package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Institution;
import com.validaya.validaya.entity.enums.InstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findBySlug(String slug);

    List<Institution> findByIsActiveTrue();

    List<Institution> findByInstitutionTypeAndIsActiveTrue(InstitutionType institutionType);

    boolean existsBySlug(String slug);
}