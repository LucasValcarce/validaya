package com.validaya.validaya.repository;

import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdentification(String identification);

    boolean existsByEmail(String email);

    boolean existsByIdentification(String identification);

    long countByUserType(UserType userType);
}