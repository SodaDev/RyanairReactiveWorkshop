package io.workshop.tickets;

import com.ryanair.aws.xray.reactive.XRayControllerSegmentBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

import static org.springframework.http.HttpMethod.OPTIONS;


@EnableHystrix
@SpringBootApplication
public class TicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsApplication.class, args);
    }

    @Bean
    public WebFilter webFilter() {
        return (exchange, chain) -> {
            if (!OPTIONS.equals(exchange.getRequest().getMethod())) {
                new XRayControllerSegmentBuilder().createControllerSegment("Tickets Webflux", exchange);
            }

            return chain.filter(exchange);
        };
    }
}
