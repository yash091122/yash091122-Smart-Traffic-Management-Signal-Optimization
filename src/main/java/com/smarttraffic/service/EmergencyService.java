package com.smarttraffic.service;

import com.smarttraffic.config.EmergencyProperties;
import com.smarttraffic.entity.EmergencyEvent;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.repository.EmergencyEventRepository;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyEventRepository emergencyEventRepository;
    private final EmergencyProperties emergencyProperties;
    private final SignalStateService signalStateService;
    private final IntersectionRepository intersectionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public EmergencyEvent activate(List<Long> routeIntersectionIds) {
        emergencyEventRepository.findByActiveTrue().forEach(e -> {
            e.setActive(false);
            emergencyEventRepository.save(e);
        });
        EmergencyEvent ev = new EmergencyEvent();
        ev.setActive(true);
        ev.setRouteIntersectionIds(routeIntersectionIds);
        ev.setEndsAt(Instant.now().plusSeconds(emergencyProperties.getHoldSeconds()));
        ev = emergencyEventRepository.save(ev);

        long until = System.currentTimeMillis() + emergencyProperties.getHoldSeconds() * 1000L;
        for (Long id : routeIntersectionIds) {
            if (intersectionRepository.existsById(id)) {
                signalStateService.setPhase(id, SignalPhase.GREEN, until);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Emergency corridor active");
        payload.put("intersectionIds", routeIntersectionIds);
        payload.put("endsAt", ev.getEndsAt().toString());
        messagingTemplate.convertAndSend("/topic/emergency", payload);

        return ev;
    }

    public Set<Long> activeRouteIntersectionIds() {
        Instant now = Instant.now();
        return emergencyEventRepository.findByActiveTrue().stream()
                .filter(e -> e.getEndsAt() != null && e.getEndsAt().isAfter(now))
                .flatMap(e -> e.getRouteIntersectionIds().stream())
                .collect(Collectors.toSet());
    }

    public long activeEmergencyEventCount() {
        Instant now = Instant.now();
        return emergencyEventRepository.findByActiveTrue().stream()
                .filter(e -> e.getEndsAt() != null && e.getEndsAt().isAfter(now))
                .count();
    }

    @Transactional
    public void deactivateExpired() {
        Instant now = Instant.now();
        for (EmergencyEvent e : emergencyEventRepository.findByActiveTrue()) {
            if (e.getEndsAt() != null && !e.getEndsAt().isAfter(now)) {
                e.setActive(false);
                emergencyEventRepository.save(e);
            }
        }
    }
}
