package com.smarttraffic.repository;

import com.smarttraffic.entity.EmergencyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyEventRepository extends JpaRepository<EmergencyEvent, Long> {

    List<EmergencyEvent> findByActiveTrue();
}
