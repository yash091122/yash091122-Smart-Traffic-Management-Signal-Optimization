package com.smarttraffic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "intersections")
@Getter
@Setter
@NoArgsConstructor
public class Intersection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private int laneCount = 2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SignalPhase currentPhase = SignalPhase.RED;

    @Column(nullable = false)
    private int greenDuration = 25;

    @Column(nullable = false)
    private int redDuration = 30;

    @Column(nullable = false)
    private int vehicleCount = 0;

    /** Main arterial for rush-hour patterns (higher volume during peaks). */
    @Column(nullable = false)
    private boolean mainRoad = true;
}
