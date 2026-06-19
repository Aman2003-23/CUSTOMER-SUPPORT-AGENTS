package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates the {@code X-Ingest-Secret} header on inbound email webhooks.
 *
 * Only protects {@code POST /api/tickets/ingest}. If {@code tickets.ingest.secret}
 * is empty (dev mode), the filter is a no-op and lets requests through — this
 * matches the existing "permitAll" SecurityConfig posture during development.
 *
 * When the property is set, the header must match exactly. Otherwise: 401.
 */
@Component
public class IngestSecretFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IngestSecretFilter.class);
    private static final String INGEST_PATH = "/api/tickets/ingest";
    private static final String HEADER_NAME = "X-Ingest-Secret";

    private final PathPatternRequestMatcher matcher =
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, INGEST_PATH);
    private final String configuredSecret;

    public IngestSecretFilter(@Value("${tickets.ingest.secret:}") String configuredSecret) {
        this.configuredSecret = configuredSecret == null ? "" : configuredSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!matcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (configuredSecret.isBlank()) {
            // Dev mode: secret unset, allow request through with a warning so
            // operators notice this in logs before going to production.
            log.warn("Ingest secret is not configured (TICKETS_INGEST_SECRET is empty). "
                    + "Accepting webhook on {} without authentication — dev mode only.",
                    INGEST_PATH);
            filterChain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER_NAME);
        if (provided == null || !constantTimeEquals(provided, configuredSecret)) {
            log.warn("Rejecting ingest webhook on {}: missing or invalid X-Ingest-Secret", INGEST_PATH);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid X-Ingest-Secret\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}