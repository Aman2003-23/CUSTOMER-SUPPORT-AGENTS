package com.example.demo.service;

import com.example.demo.dto.PageResponse;
import com.example.demo.dto.TicketResponse;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketCategory;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.TicketRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> list(
            List<TicketStatus> statuses,
            TicketCategory category,
            String search,
            LocalDate fromDate,
            LocalDate toDate,
            String sort,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sortSpec = parseSort(sort);

        Pageable pageable = PageRequest.of(safePage, safeSize, sortSpec);
        Specification<Ticket> spec = buildSpec(statuses, category, search, fromDate, toDate);

        Page<Ticket> result = ticketRepository.findAll(spec, pageable);
        return PageResponse.from(result, TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse get(Long id) {
        Ticket ticket = ticketRepository.findByIdWithMessages(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: id=" + id));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse changeStatus(Long id, TicketStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("status is required");
        }
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: id=" + id));
        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.from(saved);
    }

    private static Specification<Ticket> buildSpec(
            List<TicketStatus> statuses,
            TicketCategory category,
            String search,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (statuses != null && !statuses.isEmpty()) {
                preds.add(root.get("status").in(statuses));
            }
            if (category != null) {
                preds.add(cb.equal(root.get("category"), category));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase().trim() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), like),
                        cb.like(cb.lower(root.get("customerEmail")), like)
                ));
            }
            if (fromDate != null) {
                preds.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        fromDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
            }
            if (toDate != null) {
                preds.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1)));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        switch (field) {
            case "createdAt":
            case "updatedAt":
                return Sort.by(dir, field);
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}