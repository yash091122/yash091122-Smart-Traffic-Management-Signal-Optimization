package com.smarttraffic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.signal")
public class SignalProperties {
    private int baseGreenSeconds = 25;
    private int bonusSecondsPerThreshold = 5;
    private int vehicleThreshold = 10;
    private int minGreenSeconds = 15;
    private int maxGreenSeconds = 90;
    private int defaultRedSeconds = 30;
    private int violationWaitSeconds = 120;
}
