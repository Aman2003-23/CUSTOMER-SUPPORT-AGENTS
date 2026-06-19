package com.example.demo.email;

import com.example.demo.dto.OutboundEmail;

/**
 * Provider-agnostic outbound email sender. The application persists a message
 * first, then calls {@link #send(OutboundEmail)} to dispatch it. Implementations
 * translate the {@link OutboundEmail} into the provider's API call.
 */
public interface EmailSender {

    /**
     * Sends the email. Returns the provider's Message-ID on success.
     * Throws on any send failure — caller is responsible for marking the
     * {@code TicketMessage} as {@code FAILED} with the error message.
     */
    SendResult send(OutboundEmail email);
}