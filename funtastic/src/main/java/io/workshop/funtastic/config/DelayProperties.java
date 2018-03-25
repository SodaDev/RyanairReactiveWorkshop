package io.workshop.funtastic.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "delay")
public class DelayProperties {
    private Duration concert = Duration.ZERO;
    private Duration exhibition = Duration.ZERO;
    private Duration sport = Duration.ZERO;
}
