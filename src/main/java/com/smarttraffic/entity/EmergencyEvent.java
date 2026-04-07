package com.smarttraffic.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emergency_events")
@Getter
@Setter
@NoArgsConstructor
public class EmergencyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "emergency_route_intersections", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "intersection_id")
    private List<Long> routeIntersectionIds = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant endsAt;
}
