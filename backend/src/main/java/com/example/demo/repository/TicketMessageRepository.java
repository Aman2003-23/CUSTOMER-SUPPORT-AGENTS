package com.example.demo.repository;

import com.example.demo.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    Optional<TicketMessage> findByExternalMessageId(String externalMessageId);

    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}