package com.smarttraffic.service;

import com.smarttraffic.entity.Intersection;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TrafficSimulatorService {

    private final IntersectionRepository intersectionRepository;
    private final SignalStateService signalStateService;

    private volatile boolean autoSimulate = true;

    public void setAutoSimulate(boolean on) {
        this.autoSimulate = on;
    }

    public boolean isAutoSimulate() {
        return autoSimulate;
    }

    @Transactional(readOnly = true)
    public void tickSimulation() {
        if (!autoSimulate) {
            return;
        }
        LocalTime t = LocalTime.now(ZoneId.systemDefault());
        int hour = t.getHour();
        boolean morningPeak = hour >= 7 && hour < 9;
        boolean eveningPeak = hour >= 17 && hour < 20;

        List<Intersection> all = intersectionRepository.findAll();
        for (Intersection i : all) {
            int base = ThreadLocalRandom.current().nextInt(2, 25);
            int count = base;
            if (morningPeak) {
                count = i.isMainRoad() ? base + ThreadLocalRandom.current().nextInt(15, 45) : Math.max(0, base - 10);
            } else if (eveningPeak) {
                count = i.isMainRoad() ? base + ThreadLocalRandom.current().nextInt(10, 40) : base + ThreadLocalRandom.current().nextInt(5, 25);
            } else {
                count = base + ThreadLocalRandom.current().nextInt(-3, 8);
            }
            count = Math.max(0, Math.min(200, count));
            signalStateService.setVehicleCount(i.getId(), count);
            i.setVehicleCount(count);
        }
        intersectionRepository.saveAll(all);
    }

    @Transactional
    public void setManualCount(Long intersectionId, int count) {
        intersectionRepository.findById(intersectionId).ifPresent(i -> {
            int c = Math.max(0, Math.min(500, count));
            i.setVehicleCount(c);
            intersectionRepository.save(i);
            signalStateService.setVehicleCount(intersectionId, c);
        });
    }
}
