package com.smarttraffic.bootstrap;

import com.smarttraffic.repository.IntersectionRepository;
import com.smarttraffic.service.SignalStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RedisSyncRunner implements ApplicationRunner {

    private final IntersectionRepository intersectionRepository;
    private final SignalStateService signalStateService;

    @Override
    public void run(ApplicationArguments args) {
        intersectionRepository.findAll().forEach(i -> {
            signalStateService.syncFromIntersection(i);
            log.debug("Synced intersection {} to Redis", i.getId());
        });
    }
}
