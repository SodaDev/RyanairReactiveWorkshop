package io.workshop.tickets.rest;

import io.workshop.tickets.model.RyanairTicketDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController("/tickets")
public class RyanairTicketsController {

    private final WebClient funtasticClient = WebClient.builder()
            .baseUrl("http://localhost:8080/api/v1/")
            .build();

    @GetMapping
    public Flux<RyanairTicketDto> loadTickets() {
        return Flux.just("concert", "exhibition", "sport")
                .flatMap(path -> funtasticClient
                        .get()
                        .uri(path)
                        .retrieve()
                        .bodyToFlux(RyanairTicketDto.class)
                );
    }
}
