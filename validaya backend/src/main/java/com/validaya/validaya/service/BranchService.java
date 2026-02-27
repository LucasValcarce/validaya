package com.validaya.validaya.service;

import com.validaya.validaya.entity.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchService {

    Branch create(Branch branch);

    Branch update(Long id, Branch updated);

    Optional<Branch> findById(Long id);

    List<Branch> findByInstitution(Long institutionId);

    List<Branch> findByCity(String city);

    void deactivate(Long id);

    void activate(Long id);
}
