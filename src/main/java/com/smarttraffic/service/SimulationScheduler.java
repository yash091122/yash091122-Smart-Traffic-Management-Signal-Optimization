package com.smarttraffic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationScheduler {

    private final TrafficSimulatorService trafficSimulatorService;

    @Scheduled(fixedRate = 8000)
    public void runSimulationTick() {
        trafficSimulatorService.tickSimulation();
    }
}
