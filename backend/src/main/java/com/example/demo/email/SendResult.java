package com.example.demo.email;

/**
 * Result of a successful outbound send. {@code messageId} is the provider's
 * Message-ID (or a synthetic one for NoOp). The application persists this on
 * {@code TicketMessage.externalMessageId} so the customer's next reply threads
 * back to the same ticket via {@code In-Reply-To}.
 */
public class SendResult {

    private final String messageId;
    private final boolean skipped;

    public SendResult(String messageId, boolean skipped) {
        this.messageId = messageId;
        this.skipped = skipped;
    }

    public String getMessageId() { return messageId; }

    public boolean isSkipped() { return skipped; }
}