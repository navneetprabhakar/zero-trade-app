package com.zerotrade.dashboard.controller;

import com.zerotrade.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;

@Controller
@EnableScheduling
public class DashboardController {

    private final DashboardService dashboardService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("portfolio", dashboardService.getPortfolioSummary());
        return "dashboard";
    }

    @GetMapping("/api/stream")
    public SseEmitter streamAgentActivity() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        
        return emitter;
    }

    @Scheduled(fixedRate = 2000)
    public void pushLogs() {
        String log = dashboardService.getLatestLog();
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(log));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        
        emitters.removeAll(deadEmitters);
    }
}
