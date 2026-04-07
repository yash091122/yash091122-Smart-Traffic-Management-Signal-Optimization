package com.smarttraffic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Profile("local")
@Primary
@RequiredArgsConstructor
public class InMemorySignalStateService implements SignalStateService {

    private static final String KEY_PREFIX = "st:signal:";

    private final ObjectMapper objectMapper;
    private final IntersectionRepository intersectionRepository;

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> store = new ConcurrentHashMap<>();

    @Override
    public void syncFromIntersection(Intersection i) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("id", String.valueOf(i.getId()));
        map.put("name", i.getName());
        map.put("lat", String.valueOf(i.getLatitude()));
        map.put("lng", String.valueOf(i.getLongitude()));
        map.put("laneCount", String.valueOf(i.getLaneCount()));
        map.put("phase", i.getCurrentPhase().name());
        map.put("green", String.valueOf(i.getGreenDuration()));
        map.put("red", String.valueOf(i.getRedDuration()));
        map.put("yellow", "5");
        map.put("vehicleCount", String.valueOf(i.getVehicleCount()));
        map.put("phaseEndsAt", String.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(i.getGreenDuration())));
        store.put(KEY_PREFIX + i.getId(), map);
    }

    @Override
    public void setVehicleCount(Long id, int count) {
        bucket(id).put("vehicleCount", String.valueOf(count));
    }

    @Override
    public void setDurations(Long id, int green, int red) {
        ConcurrentHashMap<String, String> b = bucket(id);
        b.put("green", String.valueOf(green));
        b.put("red", String.valueOf(red));
    }

    @Override
    public void setPhase(Long id, SignalPhase phase, long phaseEndsAtEpochMs) {
        ConcurrentHashMap<String, String> b = bucket(id);
        b.put("phase", phase.name());
        b.put("phaseEndsAt", String.valueOf(phaseEndsAtEpochMs));
    }

    @Override
    public Optional<LiveSignalDto> getLive(Long id) {
        ConcurrentHashMap<String, String> entries = store.get(KEY_PREFIX + id);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDto(entries));
    }

    @Override
    public List<LiveSignalDto> getAllLive() {
        List<LiveSignalDto> list = new ArrayList<>();
        for (Intersection i : intersectionRepository.findAll()) {
            getLive(i.getId()).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public long getPhaseEndsAt(Long id) {
        String v = bucket(id).get("phaseEndsAt");
        if (v == null) {
            return System.currentTimeMillis();
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return System.currentTimeMillis();
        }
    }

    @Override
    public String exportJson(List<LiveSignalDto> dtos) {
        try {
            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private ConcurrentHashMap<String, String> bucket(Long id) {
        return store.computeIfAbsent(KEY_PREFIX + id, k -> new ConcurrentHashMap<>());
    }

    private LiveSignalDto toDto(Map<String, String> entries) {
        long lid = Long.parseLong(entries.getOrDefault("id", "0"));
        String name = entries.getOrDefault("name", "");
        double lat = Double.parseDouble(entries.getOrDefault("lat", "0"));
        double lng = Double.parseDouble(entries.getOrDefault("lng", "0"));
        SignalPhase phase = SignalPhase.valueOf(entries.getOrDefault("phase", "RED"));
        int vc = Integer.parseInt(entries.getOrDefault("vehicleCount", "0"));
        int green = Integer.parseInt(entries.getOrDefault("green", "25"));
        int red = Integer.parseInt(entries.getOrDefault("red", "30"));
        int lanes = Integer.parseInt(entries.getOrDefault("laneCount", "2"));
        return new LiveSignalDto(lid, name, lat, lng, phase, vc, green, red, lanes);
    }
}
