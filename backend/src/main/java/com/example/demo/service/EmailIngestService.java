package com.example.demo.service;

import com.example.demo.dto.EmailPayload;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.MessageAuthor;
import com.example.demo.model.MessageKind;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketMessage;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.TicketMessageRepository;
import com.example.demo.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parses an inbound {@link EmailPayload}, finds or creates the matching ticket,
 * appends the customer's message, and categorizes the result.
 *
 * Threading rules:
 *   - If {@code inReplyTo} matches an existing {@code TicketMessage.externalMessageId},
 *     the new message is appended to that ticket and status is left as-is.
 *   - If {@code references} match, we use the first one that resolves.
 *   - Otherwise we create a new ticket with status NEW.
 *
 * Internal notes / out-of-band: a message is considered "first in a thread"
 * iff no threading headers match.
 */
@Service
public class EmailIngestService {

    private static final Logger log = LoggerFactory.getLogger(EmailIngestService.class);

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final KeywordCategorizer categorizer;

    public EmailIngestService(TicketRepository ticketRepository,
                              TicketMessageRepository messageRepository,
                              KeywordCategorizer categorizer) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.categorizer = categorizer;
    }

    @Transactional
    public Ticket ingest(EmailPayload payload) {
        String body = resolveBody(payload);
        if (body.isBlank()) {
            throw new IllegalArgumentException("Email body is required (textBody or htmlBody).");
        }

        TicketMessage existing = findThreadMatch(payload);

        if (existing != null) {
            return appendCustomerMessage(existing.getTicket(), payload, body);
        }
        return createTicket(payload, body);
    }

    private Ticket createTicket(EmailPayload payload, String body) {
        Ticket ticket = new Ticket();
        ticket.setSubject(payload.getSubject());
        ticket.setCustomerEmail(payload.getFrom());
        ticket.setDescription(truncate(body, 4000));
        ticket.setStatus(TicketStatus.NEW);

        KeywordCategorizer.Result cat = categorizer.categorize(payload.getSubject(), body);
        ticket.setCategory(cat.category());
        ticket.setKeywords(new java.util.ArrayList<>(cat.keywords()));

        TicketMessage message = new TicketMessage();
        message.setAuthor(MessageAuthor.CUSTOMER);
        message.setKind(MessageKind.REPLY);
        message.setExternalMessageId(payload.getMessageId());
        message.setBody(body);

        ticket.addMessage(message);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ingested new ticket id={} from={} category={} keywords={}",
                saved.getId(), payload.getFrom(), cat.category(), cat.keywords());
        return saved;
    }

    private Ticket appendCustomerMessage(Ticket ticket, EmailPayload payload, String body) {
        TicketMessage message = new TicketMessage();
        message.setAuthor(MessageAuthor.CUSTOMER);
        message.setKind(MessageKind.REPLY);
        message.setExternalMessageId(payload.getMessageId());
        message.setBody(body);
        ticket.addMessage(message);

        // Re-categorize on the latest subject + body so follow-ups move categories too.
        StringBuilder allBody = new StringBuilder();
        for (TicketMessage m : ticket.getMessages()) {
            allBody.append(m.getBody()).append(' ');
        }
        KeywordCategorizer.Result cat = categorizer.categorize(
                payload.getSubject() == null ? ticket.getSubject() : payload.getSubject(),
                allBody.toString());
        ticket.setCategory(cat.category());
        ticket.setKeywords(new java.util.ArrayList<>(cat.keywords()));

        Ticket saved = ticketRepository.save(ticket);
        log.info("Appended customer message to ticket id={} from={}", saved.getId(), payload.getFrom());
        return saved;
    }

    private TicketMessage findThreadMatch(EmailPayload payload) {
        String inReplyTo = payload.getInReplyTo();
        if (inReplyTo != null && !inReplyTo.isBlank()) {
            TicketMessage match = messageRepository.findByExternalMessageId(inReplyTo.trim()).orElse(null);
            if (match != null) {
                return match;
            }
        }
        if (payload.getReferences() != null) {
            for (String ref : payload.getReferences()) {
                if (ref == null || ref.isBlank()) continue;
                TicketMessage match = messageRepository.findByExternalMessageId(ref.trim()).orElse(null);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static String resolveBody(EmailPayload payload) {
        if (payload.getTextBody() != null && !payload.getTextBody().isBlank()) {
            return payload.getTextBody();
        }
        if (payload.getHtmlBody() != null && !payload.getHtmlBody().isBlank()) {
            return stripHtml(payload.getHtmlBody());
        }
        return "";
    }

    /**
     * Lightweight HTML stripping. Good enough for the no-attachments case;
     * if we ever need stricter handling, swap in JSoup.
     */
    static String stripHtml(String html) {
        String stripped = html.replaceAll("<[^>]+>", " ");
        stripped = stripped.replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"");
        return stripped.replaceAll("\\s+", " ").trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}