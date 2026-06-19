package com.example.demo.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider-agnostic outbound email. Carried into the {@code EmailSender} interface;
 * concrete implementations translate this into the provider's SDK call.
 */
public class OutboundEmail {

    private String to;
    private String from;
    private String subject;
    private String textBody;
    private String htmlBody;
    private String inReplyTo;
    private String references;
    private Map<String, String> customHeaders = new HashMap<>();

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

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

    public String getReferences() { return references; }
    public void setReferences(String references) { this.references = references; }

    public Map<String, String> getCustomHeaders() { return customHeaders; }
    public void setCustomHeaders(Map<String, String> customHeaders) { this.customHeaders = customHeaders; }
}