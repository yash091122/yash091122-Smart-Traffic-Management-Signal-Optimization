package com.smarttraffic.controller;

import com.smarttraffic.dto.IntersectionRequest;
import com.smarttraffic.dto.LiveSignalDto;
import com.smarttraffic.dto.PhaseOverrideRequest;
import com.smarttraffic.entity.Intersection;
import com.smarttraffic.repository.IntersectionRepository;
import com.smarttraffic.service.SignalStateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/intersections")
@RequiredArgsConstructor
public class IntersectionController {

    private final IntersectionRepository intersectionRepository;
    private final SignalStateService signalStateService;

    @GetMapping
    public List<Intersection> list() {
        return intersectionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Intersection> get(@PathVariable Long id) {
        return intersectionRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Intersection create(@Valid @RequestBody IntersectionRequest req) {
        Intersection i = new Intersection();
        apply(i, req);
        i = intersectionRepository.save(i);
        signalStateService.syncFromIntersection(i);
        return i;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Intersection> update(@PathVariable Long id, @Valid @RequestBody IntersectionRequest req) {
        return intersectionRepository.findById(id).map(i -> {
            apply(i, req);
            i = intersectionRepository.save(i);
            signalStateService.syncFromIntersection(i);
            return ResponseEntity.ok(i);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!intersectionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        intersectionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/override-phase")
    public ResponseEntity<LiveSignalDto> manualOverride(@PathVariable Long id, @Valid @RequestBody PhaseOverrideRequest body) {
        return intersectionRepository.findById(id).map(i -> {
            long until = System.currentTimeMillis() + 120_000L;
            signalStateService.setPhase(id, body.phase(), until);
            i.setCurrentPhase(body.phase());
            intersectionRepository.save(i);
            return ResponseEntity.ok(signalStateService.getLive(id).orElseThrow());
        }).orElse(ResponseEntity.notFound().build());
    }

    private void apply(Intersection i, IntersectionRequest req) {
        i.setName(req.name());
        i.setLatitude(req.latitude());
        i.setLongitude(req.longitude());
        if (req.laneCount() != null) {
            i.setLaneCount(req.laneCount());
        }
        if (req.currentPhase() != null) {
            i.setCurrentPhase(req.currentPhase());
        }
        if (req.greenDuration() != null) {
            i.setGreenDuration(req.greenDuration());
        }
        if (req.redDuration() != null) {
            i.setRedDuration(req.redDuration());
        }
        if (req.vehicleCount() != null) {
            i.setVehicleCount(req.vehicleCount());
        }
        if (req.mainRoad() != null) {
            i.setMainRoad(req.mainRoad());
        }
    }
}
