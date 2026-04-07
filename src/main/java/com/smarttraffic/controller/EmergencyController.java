package com.smarttraffic.controller;

import com.smarttraffic.entity.EmergencyEvent;
import com.smarttraffic.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    public record ActivateRequest(List<Long> routeIntersectionIds) {}

    @PostMapping("/activate")
    public EmergencyEvent activate(@RequestBody ActivateRequest body) {
        return emergencyService.activate(body.routeIntersectionIds());
    }
}
