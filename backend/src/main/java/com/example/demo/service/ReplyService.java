package com.example.demo.service;

import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.OutboundEmail;
import com.example.demo.email.EmailSender;
import com.example.demo.email.SendResult;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.MessageAuthor;
import com.example.demo.model.MessageKind;
import com.example.demo.model.SendStatus;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketMessage;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.TicketMessageRepository;
import com.example.demo.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Persists agent replies and internal notes, then dispatches replies
 * through the {@link EmailSender}. Persist-first: the message is durable
 * in the database even if the send fails; on failure the message is
 * marked {@code FAILED} with the error surfaced in the UI for retry.
 */
@Service
public class ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyService.class);

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final EmailSender emailSender;
    private final String fromAddress;

    public ReplyService(TicketRepository ticketRepository,
                        TicketMessageRepository messageRepository,
                        EmailSender emailSender,
                        String supportFromAddress) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.emailSender = emailSender;
        this.fromAddress = supportFromAddress;
    }

    @Transactional
    public MessageResponse sendReply(Long ticketId, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Reply body is required");
        }
        Ticket ticket = ticketRepository.findByIdWithMessages(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: id=" + ticketId));

        TicketMessage message = new TicketMessage();
        message.setAuthor(MessageAuthor.AGENT);
        message.setKind(MessageKind.REPLY);
        message.setBody(body);
        // Preliminary ID; replaced on success with the SendGrid message ID.
        message.setExternalMessageId("<" + UUID.randomUUID() + "@support.local>");
        message.setSendStatus(SendStatus.PENDING);
        ticket.addMessage(message);

        // Auto-transition: NEW -> OPEN, PENDING -> OPEN; otherwise leave alone.
        TicketStatus current = ticket.getStatus();
        if (current == TicketStatus.NEW || current == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        // Persist before sending so the message is durable even on send failure.
        ticketRepository.saveAndFlush(ticket);
        TicketMessage persisted = ticket.getMessages().get(ticket.getMessages().size() - 1);

        OutboundEmail outbound = buildOutboundEmail(ticket, persisted);
        try {
            SendResult result = emailSender.send(outbound);
            persisted.setExternalMessageId(result.getMessageId());
            persisted.setSendStatus(result.isSkipped() ? SendStatus.SKIPPED : SendStatus.SENT);
            persisted.setSendError(null);
        } catch (Exception ex) {
            log.warn("Reply send failed for ticket id={} messageId={}: {}",
                    ticket.getId(), persisted.getExternalMessageId(), ex.getMessage());
            persisted.setSendStatus(SendStatus.FAILED);
            String errMsg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            persisted.setSendError(truncate(errMsg, 1000));
        }

        messageRepository.save(persisted);
        ticketRepository.save(ticket);
        return MessageResponse.from(persisted);
    }

    @Transactional
    public MessageResponse addInternalNote(Long ticketId, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Note body is required");
        }
        Ticket ticket = ticketRepository.findByIdWithMessages(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: id=" + ticketId));

        TicketMessage note = new TicketMessage();
        note.setAuthor(MessageAuthor.AGENT);
        note.setKind(MessageKind.INTERNAL_NOTE);
        note.setBody(body);
        note.setSendStatus(SendStatus.NOT_APPLICABLE);
        ticket.addMessage(note);

        ticketRepository.saveAndFlush(ticket);
        TicketMessage persisted = ticket.getMessages().get(ticket.getMessages().size() - 1);
        return MessageResponse.from(persisted);
    }

    @Transactional
    public MessageResponse retrySend(Long ticketId, Long messageId) {
        TicketMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found: id=" + messageId));

        if (!message.getTicket().getId().equals(ticketId)) {
            throw new NotFoundException("Message does not belong to ticket id=" + ticketId);
        }
        if (message.getAuthor() != MessageAuthor.AGENT || message.getKind() != MessageKind.REPLY) {
            throw new IllegalArgumentException("Only agent replies can be retried");
        }

        Ticket ticket = ticketRepository.findByIdWithMessages(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: id=" + ticketId));

        message.setSendStatus(SendStatus.PENDING);
        message.setSendError(null);
        messageRepository.saveAndFlush(message);

        OutboundEmail outbound = buildOutboundEmail(ticket, message);
        try {
            SendResult result = emailSender.send(outbound);
            message.setExternalMessageId(result.getMessageId());
            message.setSendStatus(result.isSkipped() ? SendStatus.SKIPPED : SendStatus.SENT);
            message.setSendError(null);
        } catch (Exception ex) {
            message.setSendStatus(SendStatus.FAILED);
            String errMsg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            message.setSendError(truncate(errMsg, 1000));
        }
        messageRepository.save(message);
        ticketRepository.save(ticket);
        return MessageResponse.from(message);
    }

    private OutboundEmail buildOutboundEmail(Ticket ticket, TicketMessage currentMessage) {
        OutboundEmail out = new OutboundEmail();
        out.setTo(ticket.getCustomerEmail());
        out.setFrom(this.fromAddress);
        out.setSubject(ticket.getSubject() == null || ticket.getSubject().isBlank()
                ? "Support ticket"
                : "Re: " + ticket.getSubject());
        out.setTextBody(currentMessage.getBody());
        out.getCustomHeaders().put("X-Ticket-Id", String.valueOf(ticket.getId()));

        // Find the most recent message before this one with an externalMessageId
        // (i.e., one we've actually sent out or that came in with a Message-ID)
        // and use it for In-Reply-To / References threading.
        StringBuilder references = new StringBuilder();
        TicketMessage previous = null;
        List<TicketMessage> messages = ticket.getMessages();
        int currentIdx = messages.indexOf(currentMessage);
        for (int i = currentIdx - 1; i >= 0; i--) {
            TicketMessage m = messages.get(i);
            if (m.getExternalMessageId() != null && !m.getExternalMessageId().isBlank()
                    && !m.getExternalMessageId().equals(currentMessage.getExternalMessageId())) {
                previous = m;
                break;
            }
        }
        if (previous != null) {
            out.setInReplyTo(previous.getExternalMessageId());
            references.append(previous.getExternalMessageId());
        }
        // Walk further back to build a chain, but cap at a reasonable length.
        int cap = 5;
        for (int i = (previous == null ? currentIdx - 1 : messages.indexOf(previous) - 1); i >= 0 && cap > 0; i--) {
            TicketMessage m = messages.get(i);
            if (m.getExternalMessageId() != null && !m.getExternalMessageId().isBlank()) {
                references.insert(0, m.getExternalMessageId() + " ");
                cap--;
            }
        }
        if (references.length() > 0) {
            out.setReferences(references.toString().trim());
        }
        return out;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}