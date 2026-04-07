package com.smarttraffic.service;

import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.entity.RoadSegment;
import com.smarttraffic.repository.RoadSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final RoadSegmentRepository roadSegmentRepository;
    private final SignalStateService signalStateService;

    @Transactional(readOnly = true)
    public List<Long> shortestPath(Long fromIntersectionId, Long toIntersectionId) {
        if (fromIntersectionId.equals(toIntersectionId)) {
            return List.of(fromIntersectionId);
        }

        Map<Long, List<RoadSegment>> adj = new HashMap<>();
        for (RoadSegment seg : roadSegmentRepository.findAll()) {
            Long from = seg.getFromIntersection().getId();
            adj.computeIfAbsent(from, k -> new ArrayList<>()).add(seg);
        }

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> prev = new HashMap<>();
        Set<Long> visited = new HashSet<>();

        PriorityQueue<Long> pq = new PriorityQueue<>(Comparator.comparingDouble(id -> dist.getOrDefault(id, Double.POSITIVE_INFINITY)));
        dist.put(fromIntersectionId, 0.0);
        pq.add(fromIntersectionId);

        while (!pq.isEmpty()) {
            Long u = pq.poll();
            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);
            if (u.equals(toIntersectionId)) {
                break;
            }
            for (RoadSegment seg : adj.getOrDefault(u, List.of())) {
                Long v = seg.getToIntersection().getId();
                double w = seg.getBaseTravelSeconds() + estimatedWaitAt(v);
                double alt = dist.get(u) + w;
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        if (!dist.containsKey(toIntersectionId)) {
            return List.of();
        }

        List<Long> path = new ArrayList<>();
        Long cur = toIntersectionId;
        while (cur != null) {
            path.add(cur);
            if (cur.equals(fromIntersectionId)) {
                break;
            }
            cur = prev.get(cur);
        }
        if (path.isEmpty() || !path.get(path.size() - 1).equals(fromIntersectionId)) {
            return List.of();
        }
        Collections.reverse(path);
        return path;
    }

    private double estimatedWaitAt(Long intersectionId) {
        LiveSignalDto d = signalStateService.getLive(intersectionId).orElse(null);
        if (d == null) {
            return 20;
        }
        double phasePart = switch (d.phase()) {
            case RED -> d.redDuration() * 0.45;
            case YELLOW -> 4;
            case GREEN -> 2;
        };
        return phasePart + d.vehicleCount() * 0.35;
    }
}
