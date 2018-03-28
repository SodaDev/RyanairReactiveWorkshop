package io.workshop.funtastic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "failure")
public class FailureProperties {
    private Double concert = 0d;
    private Double exhibition = 0d;
    private Double sport = 0d;
}
