package com.example.demo.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the bootstrap admin user seeded at startup.
 *
 * Properties live in application.properties under the {@code app.admin} prefix.
 * Example:
 * <pre>
 *   app.admin.username=${ADMIN_USERNAME}
 *   app.admin.password=${ADMIN_PASSWORD}
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}