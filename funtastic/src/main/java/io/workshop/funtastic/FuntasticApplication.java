package io.workshop.funtastic;

import io.workshop.funtastic.config.DelayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DelayProperties.class)
public class FuntasticApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuntasticApplication.class, args);
    }
}
