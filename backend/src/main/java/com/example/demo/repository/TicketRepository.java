package com.example.demo.repository;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketCategory;
import com.example.demo.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @Query("select t from Ticket t left join fetch t.messages where t.id = :id")
    Optional<Ticket> findByIdWithMessages(@Param("id") Long id);

    Optional<Ticket> findFirstByMessagesExternalMessageId(String externalMessageId);

    // --- Dashboard aggregation queries ---

    @Query("select t.status as status, count(t) as count from Ticket t group by t.status")
    List<StatusCountProjection> countByStatusGrouped();

    @Query("select t.category as category, count(t) as count from Ticket t group by t.category")
    List<CategoryCountProjection> countByCategoryGrouped();

    @Query(value = """
            select cast(created_at as date) as day, count(*) as count
            from tickets
            where created_at >= :since
            group by cast(created_at as date)
            order by cast(created_at as date) asc
            """, nativeQuery = true)
    List<DailyCountProjection> dailyVolumeSince(@Param("since") Instant since);

    @Query("select count(t) from Ticket t where t.createdAt >= :since")
    long countCreatedSince(@Param("since") Instant since);

    @Query("select count(t) from Ticket t where t.status in :statuses")
    long countByStatusIn(@Param("statuses") Collection<TicketStatus> statuses);

    @Query(value = """
            select avg(extract(epoch from (first_reply.first_reply_at - t.created_at))) as avgSeconds,
                   count(t) as sampleCount
            from tickets t
            join (
                select m.ticket_id as ticket_id, min(m.created_at) as first_reply_at
                from ticket_messages m
                where m.author = 'AGENT' and m.kind = 'REPLY'
                group by m.ticket_id
            ) first_reply on first_reply.ticket_id = t.id
            """, nativeQuery = true)
    AverageResponseProjection computeAverageFirstReplySeconds();

    // --- Projection interfaces for aggregate results ---

    interface StatusCountProjection {
        TicketStatus getStatus();
        long getCount();
    }

    interface CategoryCountProjection {
        TicketCategory getCategory();
        long getCount();
    }

    interface DailyCountProjection {
        LocalDate getDay();
        long getCount();
    }

    interface AverageResponseProjection {
        Double getAvgSeconds();
        long getSampleCount();
    }
}