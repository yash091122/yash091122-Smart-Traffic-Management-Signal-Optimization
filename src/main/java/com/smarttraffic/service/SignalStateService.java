package com.smarttraffic.service;

import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.entity.SignalPhase;

import java.util.List;
import java.util.Optional;

public interface SignalStateService {

    void syncFromIntersection(Intersection i);

    void setVehicleCount(Long id, int count);

    void setDurations(Long id, int green, int red);

    void setPhase(Long id, SignalPhase phase, long phaseEndsAtEpochMs);

    Optional<LiveSignalDto> getLive(Long id);

    List<LiveSignalDto> getAllLive();

    long getPhaseEndsAt(Long id);

    String exportJson(List<LiveSignalDto> dtos);
}
