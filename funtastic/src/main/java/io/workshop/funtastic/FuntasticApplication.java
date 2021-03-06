package io.workshop.funtastic;

import io.workshop.funtastic.config.DelayProperties;
import io.workshop.funtastic.config.FailureProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        DelayProperties.class,
        FailureProperties.class
})
public class FuntasticApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuntasticApplication.class, args);
    }
}
