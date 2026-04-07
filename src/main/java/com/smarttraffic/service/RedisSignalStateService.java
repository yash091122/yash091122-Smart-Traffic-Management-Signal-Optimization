package com.smarttraffic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.repository.IntersectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Profile("!local")
@RequiredArgsConstructor
public class RedisSignalStateService implements SignalStateService {

    private static final String KEY_PREFIX = "st:signal:";
    private static final long KEY_TTL_DAYS = 7;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final IntersectionRepository intersectionRepository;

    @Override
    public void syncFromIntersection(Intersection i) {
        Map<String, String> map = new java.util.HashMap<>();
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
        redis.opsForHash().putAll(KEY_PREFIX + i.getId(), map);
        redis.expire(KEY_PREFIX + i.getId(), KEY_TTL_DAYS, TimeUnit.DAYS);
    }

    @Override
    public void setVehicleCount(Long id, int count) {
        redis.opsForHash().put(KEY_PREFIX + id, "vehicleCount", String.valueOf(count));
    }

    @Override
    public void setDurations(Long id, int green, int red) {
        redis.opsForHash().put(KEY_PREFIX + id, "green", String.valueOf(green));
        redis.opsForHash().put(KEY_PREFIX + id, "red", String.valueOf(red));
    }

    @Override
    public void setPhase(Long id, SignalPhase phase, long phaseEndsAtEpochMs) {
        redis.opsForHash().put(KEY_PREFIX + id, "phase", phase.name());
        redis.opsForHash().put(KEY_PREFIX + id, "phaseEndsAt", String.valueOf(phaseEndsAtEpochMs));
    }

    @Override
    public Optional<LiveSignalDto> getLive(Long id) {
        Map<Object, Object> entries = redis.opsForHash().entries(KEY_PREFIX + id);
        if (entries.isEmpty()) {
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
        Object v = redis.opsForHash().get(KEY_PREFIX + id, "phaseEndsAt");
        if (v == null) {
            return System.currentTimeMillis();
        }
        try {
            return Long.parseLong(v.toString());
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

    private LiveSignalDto toDto(Map<Object, Object> entries) {
        long id = Long.parseLong(entries.getOrDefault("id", "0").toString());
        String name = entries.getOrDefault("name", "").toString();
        double lat = Double.parseDouble(entries.getOrDefault("lat", "0").toString());
        double lng = Double.parseDouble(entries.getOrDefault("lng", "0").toString());
        SignalPhase phase = SignalPhase.valueOf(entries.getOrDefault("phase", "RED").toString());
        int vc = Integer.parseInt(entries.getOrDefault("vehicleCount", "0").toString());
        int green = Integer.parseInt(entries.getOrDefault("green", "25").toString());
        int red = Integer.parseInt(entries.getOrDefault("red", "30").toString());
        int lanes = Integer.parseInt(entries.getOrDefault("laneCount", "2").toString());
        return new LiveSignalDto(id, name, lat, lng, phase, vc, green, red, lanes);
    }
}
