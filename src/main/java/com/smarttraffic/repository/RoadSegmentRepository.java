package com.smarttraffic.repository;

import com.smarttraffic.entity.RoadSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadSegmentRepository extends JpaRepository<RoadSegment, Long> {

    List<RoadSegment> findByFromIntersectionId(Long fromId);
}
