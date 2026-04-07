package com.smarttraffic.controller;

import com.smarttraffic.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteOptimizationService routeOptimizationService;

    @GetMapping("/optimize")
    public Map<String, Object> optimize(
            @RequestParam("from") Long from,
            @RequestParam("to") Long to) {
        List<Long> path = routeOptimizationService.shortestPath(from, to);
        return Map.of(
                "from", from,
                "to", to,
                "path", path,
                "reachable", !path.isEmpty()
        );
    }
}
