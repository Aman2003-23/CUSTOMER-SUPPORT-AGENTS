package com.example.demo.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    public EmailSender emailSender(
            @Value("${sendgrid.enabled:false}") boolean sendgridEnabled,
            @Value("${sendgrid.api-key:}") String sendgridApiKey,
            @Value("${app.support.from-address:support@localhost}") String fromAddress
    ) {
        if (sendgridEnabled) {
            return new SendGridMailSender(sendgridApiKey, fromAddress);
        }
        return new NoOpEmailSender();
    }

    @Bean
    public String supportFromAddress(@Value("${app.support.from-address:support@localhost}") String fromAddress) {
        return fromAddress;
    }
}