package com.smarttraffic.controller;

import com.smarttraffic.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/wait-times")
    public Map<String, Object> waitTimes() {
        return analyticsService.averageWaitByIntersection();
    }

    @GetMapping("/peak-hours")
    public List<Map<String, Object>> peakHours() {
        return analyticsService.peakHourHeatmap();
    }

    @GetMapping("/congestion")
    public List<Map<String, Object>> congestion(
            @RequestParam(defaultValue = "24") int hours) {
        return analyticsService.congestionOverTime();
    }

    @GetMapping("/top-congested")
    public List<Map<String, Object>> topCongested(@RequestParam(defaultValue = "5") int limit) {
        return analyticsService.topCongested(limit);
    }
}
