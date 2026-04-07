package com.smarttraffic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Optional persisted named route (analytics / history). Graph edges use {@link RoadSegment}.
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_intersection_id")
    private Intersection fromIntersection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_intersection_id")
    private Intersection toIntersection;

    @Column(length = 512)
    private String pathIntersectionIds;

    @Column(nullable = false)
    private Instant computedAt = Instant.now();
}
