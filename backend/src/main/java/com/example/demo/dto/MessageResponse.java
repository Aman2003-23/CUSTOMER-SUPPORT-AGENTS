package com.example.demo.dto;

import com.example.demo.model.MessageAuthor;
import com.example.demo.model.MessageKind;
import com.example.demo.model.SendStatus;
import com.example.demo.model.TicketMessage;

import java.time.Instant;

public class MessageResponse {

    private Long id;
    private Instant createdAt;
    private MessageAuthor author;
    private MessageKind kind;
    private String externalMessageId;
    private SendStatus sendStatus;
    private String sendError;
    private String body;

    public static MessageResponse from(TicketMessage m) {
        MessageResponse r = new MessageResponse();
        r.id = m.getId();
        r.createdAt = m.getCreatedAt();
        r.author = m.getAuthor();
        r.kind = m.getKind();
        r.externalMessageId = m.getExternalMessageId();
        r.sendStatus = m.getSendStatus();
        r.sendError = m.getSendError();
        r.body = m.getBody();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

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