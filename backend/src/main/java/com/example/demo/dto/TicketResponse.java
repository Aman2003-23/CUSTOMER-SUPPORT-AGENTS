package com.example.demo.dto;

import com.example.demo.model.Ticket;
import com.example.demo.model.TicketCategory;
import com.example.demo.model.TicketMessage;
import com.example.demo.model.TicketStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TicketResponse {

    private Long id;
    private String subject;
    private String customerEmail;
    private String description;
    private TicketStatus status;
    private TicketCategory category;
    private List<String> keywords = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private List<MessageResponse> messages = new ArrayList<>();

    public static TicketResponse from(Ticket t) {
        TicketResponse r = new TicketResponse();
        r.id = t.getId();
        r.subject = t.getSubject();
        r.customerEmail = t.getCustomerEmail();
        r.description = t.getDescription();
        r.status = t.getStatus();
        r.category = t.getCategory();
        r.keywords = t.getKeywords() != null ? new ArrayList<>(t.getKeywords()) : new ArrayList<>();
        r.createdAt = t.getCreatedAt();
        r.updatedAt = t.getUpdatedAt();
        List<TicketMessage> msgs = t.getMessages() != null ? t.getMessages() : new ArrayList<>();
        r.messages = msgs.stream()
                .sorted(Comparator.comparing(m -> m.getCreatedAt() == null ? Instant.MIN : m.getCreatedAt()))
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<MessageResponse> getMessages() { return messages; }
    public void setMessages(List<MessageResponse> messages) { this.messages = messages; }
}