package com.smarttraffic.controller;

import com.smarttraffic.service.TrafficSimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final TrafficSimulatorService trafficSimulatorService;

    public record SetCountRequest(Long intersectionId, int vehicleCount) {}

    @PostMapping("/set-count")
    public Map<String, String> setCount(@RequestBody SetCountRequest req) {
        trafficSimulatorService.setManualCount(req.intersectionId(), req.vehicleCount());
        return Map.of("status", "ok");
    }

    @PostMapping("/run")
    public Map<String, Object> run(@RequestBody(required = false) Map<String, Object> body) {
        if (body != null && body.containsKey("auto")) {
            trafficSimulatorService.setAutoSimulate(Boolean.TRUE.equals(body.get("auto")));
        }
        trafficSimulatorService.tickSimulation();
        return Map.of(
                "status", "tick",
                "autoSimulate", trafficSimulatorService.isAutoSimulate()
        );
    }
}
