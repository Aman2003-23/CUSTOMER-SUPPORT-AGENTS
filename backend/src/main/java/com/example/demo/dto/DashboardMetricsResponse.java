package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Response shape for GET /api/metrics/dashboard.
 *
 * Aggregates ticket statistics for the admin dashboard. Distributions are
 * always pre-seeded with every enum value at count=0 so the UI does not
 * need to handle missing buckets.
 */
public class DashboardMetricsResponse {

    private long totalTickets;
    private long openTickets;
    private long todayTickets;
    private String averageResponseTime;
    private long responseTimeSampleCount;
    private int dailyDays;
    private List<DailyVolume> dailyVolume;
    private List<EnumCount> statusDistribution;
    private List<EnumCount> categoryDistribution;

    public static DashboardMetricsResponse create(
            long totalTickets,
            long openTickets,
            long todayTickets,
            String averageResponseTime,
            long responseTimeSampleCount,
            int dailyDays,
            List<DailyVolume> dailyVolume,
            List<EnumCount> statusDistribution,
            List<EnumCount> categoryDistribution
    ) {
        DashboardMetricsResponse r = new DashboardMetricsResponse();
        r.totalTickets = totalTickets;
        r.openTickets = openTickets;
        r.todayTickets = todayTickets;
        r.averageResponseTime = averageResponseTime;
        r.responseTimeSampleCount = responseTimeSampleCount;
        r.dailyDays = dailyDays;
        r.dailyVolume = dailyVolume;
        r.statusDistribution = statusDistribution;
        r.categoryDistribution = categoryDistribution;
        return r;
    }

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

    public long getTodayTickets() { return todayTickets; }
    public void setTodayTickets(long todayTickets) { this.todayTickets = todayTickets; }

    public String getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(String averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public long getResponseTimeSampleCount() { return responseTimeSampleCount; }
    public void setResponseTimeSampleCount(long responseTimeSampleCount) { this.responseTimeSampleCount = responseTimeSampleCount; }

    public int getDailyDays() { return dailyDays; }
    public void setDailyDays(int dailyDays) { this.dailyDays = dailyDays; }

    public List<DailyVolume> getDailyVolume() { return dailyVolume; }
    public void setDailyVolume(List<DailyVolume> dailyVolume) { this.dailyVolume = dailyVolume; }

    public List<EnumCount> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(List<EnumCount> statusDistribution) { this.statusDistribution = statusDistribution; }

    public List<EnumCount> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(List<EnumCount> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public static class DailyVolume {
        private LocalDate date;
        private long count;

        public DailyVolume() {}

        public DailyVolume(LocalDate date, long count) {
            this.date = date;
            this.count = count;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class EnumCount {
        private String key;
        private String label;
        private long count;

        public EnumCount() {}

        public EnumCount(String key, String label, long count) {
            this.key = key;
            this.label = label;
            this.count = count;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}