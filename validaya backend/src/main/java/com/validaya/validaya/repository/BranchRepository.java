package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByInstitutionIdAndIsActiveTrue(Long institutionId);

    List<Branch> findByCity(String city);
}