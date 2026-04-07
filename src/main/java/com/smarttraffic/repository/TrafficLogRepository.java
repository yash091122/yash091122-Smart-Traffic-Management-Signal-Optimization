package com.smarttraffic.repository;

import com.smarttraffic.entity.TrafficLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TrafficLogRepository extends JpaRepository<TrafficLog, Long> {

    List<TrafficLog> findByIntersectionIdOrderByLoggedAtDesc(Long intersectionId);

    @Query("SELECT AVG(t.redDurationSeconds) FROM TrafficLog t WHERE t.intersection.id = :id AND t.loggedAt >= :since")
    Double averageRedDurationSince(@Param("id") Long intersectionId, @Param("since") Instant since);

    @Query("SELECT hour(t.loggedAt), COUNT(t) FROM TrafficLog t WHERE t.loggedAt >= :since "
            + "GROUP BY hour(t.loggedAt) ORDER BY COUNT(t) DESC")
    List<Object[]> hourlyCountsSince(@Param("since") Instant since);

    List<TrafficLog> findByLoggedAtBetweenOrderByLoggedAtAsc(Instant from, Instant to);

    @Query("SELECT i.id, AVG(t.vehicleCount) FROM TrafficLog t JOIN t.intersection i WHERE t.loggedAt >= :since "
            + "GROUP BY i.id ORDER BY AVG(t.vehicleCount) DESC")
    List<Object[]> averageVehicleCountByIntersectionSince(@Param("since") Instant since);
}
