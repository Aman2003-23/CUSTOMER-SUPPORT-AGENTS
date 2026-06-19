package com.example.demo.email;

import com.example.demo.dto.OutboundEmail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * SendGrid v3 Mail Send API implementation. Threading headers
 * ({@code In-Reply-To}, {@code References}) are added to the personalization
 * so the customer's next reply lands back on the same ticket via the
 * {@code EmailIngestService} thread matcher.
 *
 * The returned {@code x-message-id} from SendGrid is wrapped in {@link SendResult}
 * and persisted on {@code TicketMessage.externalMessageId}.
 */
public class SendGridMailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SendGridMailSender.class);

    private final SendGrid sendGrid;
    private final String fromAddress;

    public SendGridMailSender(String apiKey, String fromAddress) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "SendGrid is enabled but SENDGRID_API_KEY is missing. Set sendgrid.api-key.");
        }
        this.sendGrid = new SendGrid(apiKey);
        this.fromAddress = fromAddress;
    }

    @Override
    public SendResult send(OutboundEmail email) {
        try {
            Mail mail = buildMail(email);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            int status = response.getStatusCode();

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "SendGrid send failed: status=" + status + " body=" + response.getBody());
            }

            String messageId = extractMessageId(response);
            log.info("Sent email via SendGrid: to={} status={} messageId={}",
                    email.getTo(), status, messageId);
            return new SendResult(messageId, false);
        } catch (Exception e) {
            // Wrap so the caller can decide whether to retry / mark FAILED.
            throw new IllegalStateException("SendGrid send failed: " + e.getMessage(), e);
        }
    }

    private Mail buildMail(OutboundEmail email) {
        Email from = new Email(this.fromAddress);
        Email to = new Email(email.getTo());
        String subject = email.getSubject() == null ? "" : email.getSubject();
        String textBody = email.getTextBody() == null ? "" : email.getTextBody();

        Mail mail = new Mail(from, subject, to, new Content("text/plain", textBody));

        if (email.getHtmlBody() != null && !email.getHtmlBody().isBlank()) {
            mail.addContent(new Content("text/html", email.getHtmlBody()));
        }

        // Personalization is created by the Mail constructor.
        Personalization personalization = mail.getPersonalization().get(0);
        if (email.getInReplyTo() != null && !email.getInReplyTo().isBlank()) {
            personalization.addHeader("In-Reply-To", email.getInReplyTo());
        }
        if (email.getReferences() != null && !email.getReferences().isBlank()) {
            // References is space-separated when there are multiple Message-IDs.
            personalization.addHeader("References", email.getReferences());
        }
        if (email.getCustomHeaders() != null) {
            for (Map.Entry<String, String> e : email.getCustomHeaders().entrySet()) {
                mail.addHeader(e.getKey(), e.getValue());
            }
        }

        return mail;
    }

    private String extractMessageId(Response response) {
        Map<String, String> headers = response.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        // java-http-client normalizes header keys to lowercase.
        String messageId = headers.get("x-message-id");
        if (messageId == null) {
            messageId = headers.get("X-Message-Id");
        }
        return messageId;
    }
}