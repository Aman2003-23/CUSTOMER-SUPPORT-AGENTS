package com.example.demo.service;

import com.example.demo.dto.DashboardMetricsResponse;
import com.example.demo.model.TicketCategory;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.TicketRepository.AverageResponseProjection;
import com.example.demo.repository.TicketRepository.CategoryCountProjection;
import com.example.demo.repository.TicketRepository.DailyCountProjection;
import com.example.demo.repository.TicketRepository.StatusCountProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates ticket statistics for the admin dashboard.
 *
 * All counts are computed via aggregate queries on {@link TicketRepository}
 * — no entity hydration, no N+1. Distributions are pre-seeded with every
 * enum value at count=0 so the UI never has to handle missing buckets.
 */
@Service
public class MetricsService {

    private static final List<TicketStatus> OPEN_STATUSES =
            List.of(TicketStatus.NEW, TicketStatus.OPEN, TicketStatus.PENDING);

    private final TicketRepository ticketRepository;

    public MetricsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse computeDashboard(int dailyDays) {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatusIn(OPEN_STATUSES);
        long today = ticketRepository.countCreatedSince(startOfTodayUtc());

        AverageResponseProjection avg = ticketRepository.computeAverageFirstReplySeconds();
        String averageResponseTime = formatAverage(avg);
        long sampleCount = avg != null ? avg.getSampleCount() : 0L;

        List<DashboardMetricsResponse.DailyVolume> daily = buildDailyVolume(dailyDays);
        List<DashboardMetricsResponse.EnumCount> statusDist = buildStatusDistribution();
        List<DashboardMetricsResponse.EnumCount> categoryDist = buildCategoryDistribution();

        return DashboardMetricsResponse.create(
                total, open, today,
                averageResponseTime, sampleCount,
                dailyDays, daily, statusDist, categoryDist
        );
    }

    private List<DashboardMetricsResponse.DailyVolume> buildDailyVolume(int dailyDays) {
        Instant since = LocalDate.now(ZoneOffset.UTC).minusDays(dailyDays - 1L)
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        List<DailyCountProjection> rows = ticketRepository.dailyVolumeSince(since);

        Map<LocalDate, Long> byDay = new HashMap<>();
        for (DailyCountProjection row : rows) {
            if (row.getDay() != null) {
                byDay.put(row.getDay(), row.getCount());
            }
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<DashboardMetricsResponse.DailyVolume> out = new ArrayList<>(dailyDays);
        for (int i = dailyDays - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            out.add(new DashboardMetricsResponse.DailyVolume(date, byDay.getOrDefault(date, 0L)));
        }
        return out;
    }

    private List<DashboardMetricsResponse.EnumCount> buildStatusDistribution() {
        Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
        for (StatusCountProjection row : ticketRepository.countByStatusGrouped()) {
            if (row.getStatus() != null) {
                counts.put(row.getStatus(), row.getCount());
            }
        }
        List<DashboardMetricsResponse.EnumCount> out = new ArrayList<>(TicketStatus.values().length);
        for (TicketStatus s : TicketStatus.values()) {
            out.add(new DashboardMetricsResponse.EnumCount(
                    s.name(), titleCase(s.name()), counts.getOrDefault(s, 0L)));
        }
        return out;
    }

    private List<DashboardMetricsResponse.EnumCount> buildCategoryDistribution() {
        Map<TicketCategory, Long> counts = new EnumMap<>(TicketCategory.class);
        for (CategoryCountProjection row : ticketRepository.countByCategoryGrouped()) {
            if (row.getCategory() != null) {
                counts.put(row.getCategory(), row.getCount());
            }
        }
        List<DashboardMetricsResponse.EnumCount> out = new ArrayList<>(TicketCategory.values().length);
        for (TicketCategory c : TicketCategory.values()) {
            out.add(new DashboardMetricsResponse.EnumCount(
                    c.name(), titleCase(c.name()), counts.getOrDefault(c, 0L)));
        }
        return out;
    }

    private static String formatAverage(AverageResponseProjection avg) {
        if (avg == null || avg.getAvgSeconds() == null || avg.getSampleCount() == 0) {
            return null;
        }
        return formatDuration(avg.getAvgSeconds().longValue());
    }

    /** Formats a duration in seconds as a human-readable string ("2h 15m", "45m", "12s"). */
    static String formatDuration(long seconds) {
        if (seconds <= 0) return "0s";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0 && minutes > 0) return hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return secs + "s";
    }

    private static String titleCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    }

    private static Instant startOfTodayUtc() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}