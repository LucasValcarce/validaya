package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Application;
import com.validaya.validaya.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    boolean existsById(Long id);

    Optional<Application> findByUserIdAndProcedureId(Long userId, Long procedureId);
}