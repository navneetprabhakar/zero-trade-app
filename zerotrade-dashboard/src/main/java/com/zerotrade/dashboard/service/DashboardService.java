package com.zerotrade.dashboard.service;

import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    public String getLatestLog() {
        return String.format("[%s] Agent Activity: Processing market tick...", LocalDateTime.now());
    }

    public Map<String, Object> getPortfolioSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("cash", 100000.0);
        summary.put("invested", 50000.0);
        summary.put("currentValue", 52000.0);
        summary.put("pnl", 2000.0);
        return summary;
    }
}
