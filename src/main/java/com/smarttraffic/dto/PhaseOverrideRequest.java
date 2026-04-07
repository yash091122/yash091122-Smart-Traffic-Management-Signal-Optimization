package com.smarttraffic.dto;

import com.smarttraffic.entity.SignalPhase;
import jakarta.validation.constraints.NotNull;

public record PhaseOverrideRequest(@NotNull SignalPhase phase) {}
