package com.smarttraffic.dto;

import com.smarttraffic.entity.SignalPhase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IntersectionRequest(
        @NotBlank String name,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @Min(1) @Max(16) Integer laneCount,
        SignalPhase currentPhase,
        @Min(5) @Max(180) Integer greenDuration,
        @Min(5) @Max(180) Integer redDuration,
        @Min(0) Integer vehicleCount,
        Boolean mainRoad
) {}
