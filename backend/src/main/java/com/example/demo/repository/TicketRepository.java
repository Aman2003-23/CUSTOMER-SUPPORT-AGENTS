package com.example.demo.repository;

import com.example.demo.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @Query("select t from Ticket t left join fetch t.messages where t.id = :id")
    Optional<Ticket> findByIdWithMessages(@Param("id") Long id);

    Optional<Ticket> findFirstByMessagesExternalMessageId(String externalMessageId);
}