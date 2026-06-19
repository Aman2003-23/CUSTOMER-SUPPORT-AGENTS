package com.example.demo.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ticket_messages", indexes = {
        @Index(name = "idx_ticket_message_external_id", columnList = "external_message_id")
})
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageAuthor author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageKind kind;

    @Column(name = "external_message_id", length = 512)
    private String externalMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", nullable = false, length = 32)
    private SendStatus sendStatus = SendStatus.NOT_APPLICABLE;

    @Column(name = "send_error", columnDefinition = "TEXT")
    private String sendError;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public MessageAuthor getAuthor() { return author; }
    public void setAuthor(MessageAuthor author) { this.author = author; }

    public MessageKind getKind() { return kind; }
    public void setKind(MessageKind kind) { this.kind = kind; }

    public String getExternalMessageId() { return externalMessageId; }
    public void setExternalMessageId(String externalMessageId) { this.externalMessageId = externalMessageId; }

    public SendStatus getSendStatus() { return sendStatus; }
    public void setSendStatus(SendStatus sendStatus) { this.sendStatus = sendStatus; }

    public String getSendError() { return sendError; }
    public void setSendError(String sendError) { this.sendError = sendError; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}