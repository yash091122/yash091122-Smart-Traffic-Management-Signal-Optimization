package com.smarttraffic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "traffic_logs")
@Getter
@Setter
@NoArgsConstructor
public class TrafficLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intersection_id")
    private Intersection intersection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SignalPhase phase;

    @Column(nullable = false)
    private int vehicleCount;

    @Column(nullable = false)
    private int greenDurationSeconds;

    @Column(nullable = false)
    private int redDurationSeconds;

    @Column(nullable = false, name = "logged_at")
    private Instant loggedAt = Instant.now();

    private Boolean violationFlag;
}
