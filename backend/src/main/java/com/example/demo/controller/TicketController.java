package com.example.demo.controller;

import com.example.demo.dto.EmailPayload;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.ReplyRequest;
import com.example.demo.dto.TicketResponse;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketCategory;
import com.example.demo.model.TicketStatus;
import com.example.demo.service.EmailIngestService;
import com.example.demo.service.ReplyService;
import com.example.demo.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final ReplyService replyService;
    private final EmailIngestService emailIngestService;

    public TicketController(TicketService ticketService,
                            ReplyService replyService,
                            EmailIngestService emailIngestService) {
        this.ticketService = ticketService;
        this.replyService = replyService;
        this.emailIngestService = emailIngestService;
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(value = "status", required = false) String statusCsv,
            @RequestParam(value = "category", required = false) TicketCategory category,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size
    ) {
        List<TicketStatus> statuses = parseStatuses(statusCsv);
        return ticketService.list(statuses, category, q, from, to, sort, page, size);
    }

    @GetMapping("/{id}")
    public TicketResponse detail(@PathVariable Long id) {
        return ticketService.get(id);
    }

    @PatchMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body == null ? null : body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        TicketStatus newStatus;
        try {
            newStatus = TicketStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown status: " + statusStr);
        }
        return ticketService.changeStatus(id, newStatus);
    }

    @PostMapping("/{id}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse reply(@PathVariable Long id, @Valid @RequestBody ReplyRequest body) {
        return replyService.sendReply(id, body.getBody());
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse addNote(@PathVariable Long id, @Valid @RequestBody ReplyRequest body) {
        return replyService.addInternalNote(id, body.getBody());
    }

    @PostMapping("/{id}/messages/{msgId}/retry")
    public MessageResponse retry(@PathVariable Long id, @PathVariable Long msgId) {
        return replyService.retrySend(id, msgId);
    }

    /**
     * Inbound email webhook. Protected by {@code X-Ingest-Secret} (validated upstream
     * by {@code IngestSecretFilter}). Returns 201 with the ticket that received
     * the new message.
     */
    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketResponse> ingest(@Valid @RequestBody EmailPayload payload) {
        if ((payload.getTextBody() == null || payload.getTextBody().isBlank())
                && (payload.getHtmlBody() == null || payload.getHtmlBody().isBlank())) {
            throw new IllegalArgumentException("Email body is required (textBody or htmlBody).");
        }
        Ticket ticket = emailIngestService.ingest(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponse.from(ticket));
    }

    private static List<TicketStatus> parseStatuses(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(TicketStatus::valueOf)
                .collect(Collectors.toList());
    }
}