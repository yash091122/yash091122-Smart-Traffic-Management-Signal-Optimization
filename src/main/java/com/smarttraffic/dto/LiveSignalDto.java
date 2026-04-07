package com.smarttraffic.dto;

import com.smarttraffic.entity.SignalPhase;

public record LiveSignalDto(
        Long id,
        String name,
        double latitude,
        double longitude,
        SignalPhase phase,
        int vehicleCount,
        int greenDuration,
        int redDuration,
        int laneCount
) {}
