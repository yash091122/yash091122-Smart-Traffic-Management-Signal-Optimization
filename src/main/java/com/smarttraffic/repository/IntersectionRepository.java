package com.smarttraffic.repository;

import com.smarttraffic.entity.Intersection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntersectionRepository extends JpaRepository<Intersection, Long> {
}
