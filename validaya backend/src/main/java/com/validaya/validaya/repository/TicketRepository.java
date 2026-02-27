package com.validaya.validaya.repository;

import com.validaya.validaya.entity.Ticket;
import com.validaya.validaya.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String code);

    Optional<Ticket> findByApplicationId(Long applicationId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByStatusAndExpiryAtBefore(TicketStatus status, LocalDateTime dateTime);
}