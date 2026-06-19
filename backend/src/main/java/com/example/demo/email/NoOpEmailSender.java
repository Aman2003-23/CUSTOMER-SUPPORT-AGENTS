package com.example.demo.email;

import com.example.demo.dto.OutboundEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Dev-mode fallback. Used when {@code sendgrid.enabled=false}.
 * Returns a synthetic Message-ID and logs the email so dev can see what
 * would have been sent without needing a real SendGrid account.
 */
public class NoOpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailSender.class);

    @Override
    public SendResult send(OutboundEmail email) {
        String messageId = "<" + UUID.randomUUID() + "@support.local>";
        log.warn("[NoOpEmailSender] Email sending disabled. Would have sent to={} subject={} messageId={}",
                email.getTo(), email.getSubject(), messageId);
        return new SendResult(messageId, true);
    }
}