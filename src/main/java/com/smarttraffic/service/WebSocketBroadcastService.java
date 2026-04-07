package com.smarttraffic.service;

import com.smarttraffic.dto.LiveSignalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final SignalStateService signalStateService;
    private final EmergencyService emergencyService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000)
    public void broadcastSignals() {
        var live = signalStateService.getAllLive();
        messagingTemplate.convertAndSend("/topic/signals", live);
    }

    @Scheduled(fixedRate = 5000)
    public void broadcastStats() {
        var live = signalStateService.getAllLive();
        double avgGreen = live.stream().mapToInt(LiveSignalDto::greenDuration).average().orElse(0);
        messagingTemplate.convertAndSend("/topic/stats", java.util.Map.of(
                "totalIntersections", live.size(),
                "averageGreenSeconds", avgGreen,
                "activeEmergencies", emergencyService.activeEmergencyEventCount()
        ));
    }
}
