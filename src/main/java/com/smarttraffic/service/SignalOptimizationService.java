package com.smarttraffic.service;

import com.smarttraffic.config.SignalProperties;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SignalOptimizationService {

    private final IntersectionRepository intersectionRepository;
    private final SignalStateService signalStateService;
    private final SignalProperties signalProperties;

    /** Extra green seconds after a violation recalibration (decays after one application). */
    private final Map<Long, Integer> recalibrationBonus = new ConcurrentHashMap<>();

    public void markRecalibration(Long intersectionId) {
        recalibrationBonus.put(intersectionId, 15);
    }

    @Scheduled(fixedRate = 10_000)
    @Transactional
    public void optimizeSignals() {
        for (Intersection i : intersectionRepository.findAll()) {
            int vehicleCount = signalStateService.getLive(i.getId())
                    .map(d -> d.vehicleCount())
                    .orElse(i.getVehicleCount());

            int bonus = recalibrationBonus.getOrDefault(i.getId(), 0);
            if (bonus > 0) {
                recalibrationBonus.remove(i.getId());
            }

            int threshold = Math.max(1, signalProperties.getVehicleThreshold());
            int extra = (vehicleCount / threshold) * signalProperties.getBonusSecondsPerThreshold();
            int green = signalProperties.getBaseGreenSeconds() + extra + bonus;
            green = Math.max(signalProperties.getMinGreenSeconds(), Math.min(signalProperties.getMaxGreenSeconds(), green));

            int red = signalProperties.getDefaultRedSeconds();
            if (vehicleCount < threshold / 2) {
                red = Math.max(15, red - 5);
            } else if (vehicleCount > threshold * 3) {
                red = Math.min(60, red + 10);
            }

            i.setVehicleCount(vehicleCount);
            i.setGreenDuration(green);
            i.setRedDuration(red);
            intersectionRepository.save(i);

            signalStateService.setDurations(i.getId(), green, red);
        }
    }
}
