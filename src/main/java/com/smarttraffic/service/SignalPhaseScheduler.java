package com.smarttraffic.service;

import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignalPhaseScheduler {

    private static final int YELLOW_SECONDS = 5;

    private final SignalStateService signalStateService;
    private final IntersectionRepository intersectionRepository;
    private final EmergencyService emergencyService;
    private final TrafficLogService trafficLogService;
    private final SignalOptimizationService signalOptimizationService;

    @Scheduled(fixedRate = 1000)
    public void advancePhasesAndLog() {
        emergencyService.deactivateExpired();
        var emergencyIds = emergencyService.activeRouteIntersectionIds();

        for (var i : intersectionRepository.findAll()) {
            Long id = i.getId();
            if (emergencyIds.contains(id)) {
                continue;
            }

            LiveSignalDto dto = signalStateService.getLive(id).orElse(null);
            if (dto == null) {
                signalStateService.syncFromIntersection(i);
                continue;
            }

            long now = System.currentTimeMillis();
            long endsAt = signalStateService.getPhaseEndsAt(id);
            if (now < endsAt) {
                continue;
            }

            SignalPhase current = dto.phase();
            SignalPhase next;
            int durationSeconds;
            switch (current) {
                case GREEN -> {
                    next = SignalPhase.YELLOW;
                    durationSeconds = YELLOW_SECONDS;
                }
                case YELLOW -> {
                    next = SignalPhase.RED;
                    durationSeconds = dto.redDuration();
                }
                case RED -> {
                    next = SignalPhase.GREEN;
                    durationSeconds = dto.greenDuration();
                }
                default -> {
                    next = SignalPhase.RED;
                    durationSeconds = dto.redDuration();
                }
            }

            long newEnds = now + durationSeconds * 1000L;
            signalStateService.setPhase(id, next, newEnds);

            intersectionRepository.findById(id).ifPresent(ent -> {
                ent.setCurrentPhase(next);
                intersectionRepository.save(ent);
            });

            LiveSignalDto updated = signalStateService.getLive(id).orElse(dto);
            boolean violation = trafficLogService.isViolation(updated);
            if (violation) {
                signalOptimizationService.markRecalibration(id);
            }
            trafficLogService.logStateChange(updated, violation);
        }
    }
}
