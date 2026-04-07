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

/**
 * Directed edge between intersections for route optimization (Dijkstra).
 */
@Entity
@Table(name = "road_segments")
@Getter
@Setter
@NoArgsConstructor
public class RoadSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_intersection_id")
    private Intersection fromIntersection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_intersection_id")
    private Intersection toIntersection;

    /** Base travel time in seconds (edge length proxy). */
    @Column(nullable = false)
    private int baseTravelSeconds = 30;
}
