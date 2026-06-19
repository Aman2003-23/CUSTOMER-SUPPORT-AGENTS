package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Inbound email webhook payload from the provider (SendGrid Inbound Parse or equivalent).
 * Provider-agnostic; a per-provider adapter normalizes the request into this shape.
 */
public class EmailPayload {

    /** Unique Message-ID from the inbound email; used for threading on the next reply. */
    private String messageId;

    @NotBlank
    @Email
    private String from;

    @NotBlank
    private String subject;

    /** Plain text body, preferred for storage and categorization. */
    private String textBody;

    /** Optional HTML body; stripped to text if textBody is empty. */
    private String htmlBody;

    /** Message-ID of the email this is replying to (if any). Used for thread matching. */
    private String inReplyTo;

    /** Chain of Message-IDs this message references. */
    private List<String> references = new ArrayList<>();

    /** Timestamp the provider received the message. */
    private Instant receivedAt;

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTextBody() { return textBody; }
    public void setTextBody(String textBody) { this.textBody = textBody; }

    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }

    public String getInReplyTo() { return inReplyTo; }
    public void setInReplyTo(String inReplyTo) { this.inReplyTo = inReplyTo; }

    public List<String> getReferences() { return references; }
    public void setReferences(List<String> references) { this.references = references; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}