package com.smarttraffic.service;

import com.smarttraffic.repository.IntersectionRepository;
import com.smarttraffic.repository.TrafficLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TrafficLogRepository trafficLogRepository;
    private final IntersectionRepository intersectionRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> averageWaitByIntersection() {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        Map<String, Object> out = new HashMap<>();
        for (var i : intersectionRepository.findAll()) {
            Double avgRed = trafficLogRepository.averageRedDurationSince(i.getId(), since);
            double waitEstimate = avgRed != null ? avgRed * 0.4 : 0;
            out.put(String.valueOf(i.getId()), Map.of(
                    "intersectionName", i.getName(),
                    "averageRedDurationSeconds", avgRed != null ? avgRed : 0,
                    "estimatedWaitSeconds", waitEstimate
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> peakHourHeatmap() {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> rows = trafficLogRepository.hourlyCountsSince(since);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : rows) {
            list.add(Map.of(
                    "hour", ((Number) row[0]).intValue(),
                    "eventCount", ((Number) row[1]).longValue()
            ));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> topCongested(int limit) {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Object[]> rows = trafficLogRepository.averageVehicleCountByIntersectionSince(since);
        List<Map<String, Object>> list = new ArrayList<>();
        int n = Math.min(limit, rows.size());
        for (int i = 0; i < n; i++) {
            Object[] row = rows.get(i);
            Long id = ((Number) row[0]).longValue();
            double avgVc = ((Number) row[1]).doubleValue();
            String name = intersectionRepository.findById(id).map(x -> x.getName()).orElse("?");
            list.add(Map.of(
                    "intersectionId", id,
                    "name", name,
                    "averageVehicleCount", avgVc,
                    "congestionScore", Math.min(100, avgVc * 2)
            ));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> congestionOverTime() {
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant to = Instant.now();
        var logs = trafficLogRepository.findByLoggedAtBetweenOrderByLoggedAtAsc(from, to);
        List<Map<String, Object>> buckets = new ArrayList<>();
        long step = 3600_000L;
        long start = from.toEpochMilli();
        long end = to.toEpochMilli();
        for (long t = start; t < end; t += step) {
            long t0 = t;
            long t1 = t + step;
            double sumVc = 0;
            int c = 0;
            for (var log : logs) {
                long ts = log.getLoggedAt().toEpochMilli();
                if (ts >= t0 && ts < t1) {
                    sumVc += log.getVehicleCount();
                    c++;
                }
            }
            double avg = c == 0 ? 0 : sumVc / c;
            buckets.add(Map.of(
                    "fromEpochMs", t0,
                    "averageVehicleCount", avg,
                    "sampleCount", c
            ));
        }
        return buckets;
    }
}
