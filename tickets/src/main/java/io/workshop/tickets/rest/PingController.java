package io.workshop.tickets.rest;

import com.ryanair.aws.xray.reactive.XRayWebClientFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/ping")
public class PingController {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://ticketsmvc:9090/pong")
            .filter(new XRayWebClientFilter())
            .build();

    @GetMapping
    public Mono<Map> ping(@RequestParam("times") int times) {
        return webClient
                .get()
                .uri(uri -> uri
                        .queryParam("times", times - 1)
                        .build())
                .retrieve()
                .bodyToMono(Map.class);
    }
}
