package com.smarttraffic.service;

import com.smarttraffic.config.SignalProperties;
import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.entity.TrafficLog;
import com.smarttraffic.repository.IntersectionRepository;
import com.smarttraffic.repository.TrafficLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrafficLogService {

    private final TrafficLogRepository trafficLogRepository;
    private final IntersectionRepository intersectionRepository;
    private final SignalProperties signalProperties;

    @Transactional
    public void logStateChange(LiveSignalDto dto, boolean violation) {
        Intersection i = intersectionRepository.findById(dto.id()).orElse(null);
        if (i == null) {
            return;
        }
        TrafficLog log = new TrafficLog();
        log.setIntersection(i);
        log.setPhase(dto.phase());
        log.setVehicleCount(dto.vehicleCount());
        log.setGreenDurationSeconds(dto.greenDuration());
        log.setRedDurationSeconds(dto.redDuration());
        log.setViolationFlag(violation);
        trafficLogRepository.save(log);
    }

    public boolean isViolation(LiveSignalDto dto) {
        if (dto.phase() != SignalPhase.RED) {
            return false;
        }
        int waitProxy = dto.redDuration() + dto.vehicleCount() * 2;
        return waitProxy >= signalProperties.getViolationWaitSeconds();
    }
}
