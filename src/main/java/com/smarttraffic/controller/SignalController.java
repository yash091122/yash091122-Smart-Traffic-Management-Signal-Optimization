package com.smarttraffic.controller;

import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.service.SignalStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
public class SignalController {

    private final SignalStateService signalStateService;

    @GetMapping("/live")
    public List<LiveSignalDto> live() {
        return signalStateService.getAllLive();
    }
}
