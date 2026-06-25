package com.example.demo.controller;

import com.example.demo.dto.DashboardMetricsResponse;
import com.example.demo.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final int DEFAULT_DAYS = 14;
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 90;

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/dashboard")
    public DashboardMetricsResponse dashboard(
            @RequestParam(value = "days", required = false, defaultValue = "14") int days
    ) {
        int clamped = Math.max(MIN_DAYS, Math.min(MAX_DAYS, days <= 0 ? DEFAULT_DAYS : days));
        return metricsService.computeDashboard(clamped);
    }
}