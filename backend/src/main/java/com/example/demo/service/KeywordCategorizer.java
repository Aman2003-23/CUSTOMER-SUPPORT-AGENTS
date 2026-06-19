package com.example.demo.service;

import com.example.demo.model.TicketCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless keyword categorizer. Runs at ingest time on (subject + body)
 * and tags the resulting {@link com.example.demo.model.Ticket} with the
 * best-matching category plus the keywords that matched, so the admin
 * dashboard's filter bar can show them as chips.
 */
@Component
public class KeywordCategorizer {

    private static final Map<TicketCategory, List<String>> KEYWORDS = Map.of(
            TicketCategory.BILLING, List.of("invoice", "refund", "payment", "charge", "billing", "receipt"),
            TicketCategory.TECHNICAL, List.of("error", "bug", "crash", "broken", "not working", "issue", "stack trace"),
            TicketCategory.ACCOUNT, List.of("login", "password", "account", "signup", "sign up", "access", "2fa")
    );

    public Result categorize(String subject, String body) {
        String haystack = ((subject == null ? "" : subject) + " " + (body == null ? "" : body)).toLowerCase();

        Map<TicketCategory, List<String>> hits = new LinkedHashMap<>();
        for (Map.Entry<TicketCategory, List<String>> entry : KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (haystack.contains(kw)) {
                    hits.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(kw);
                }
            }
        }

        TicketCategory best = TicketCategory.GENERAL;
        int bestCount = 0;
        for (Map.Entry<TicketCategory, List<String>> e : hits.entrySet()) {
            int count = e.getValue().size();
            if (count > bestCount) {
                best = e.getKey();
                bestCount = count;
            }
        }

        List<String> tags = hits.values().stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .toList();

        return new Result(best, tags);
    }

    public record Result(TicketCategory category, List<String> keywords) { }
}