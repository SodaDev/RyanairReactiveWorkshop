package io.workshop.funtastic.rest;

import com.github.javafaker.Faker;
import io.workshop.funtastic.config.DelayProperties;
import io.workshop.funtastic.config.FailureProperties;
import io.workshop.funtastic.model.FunEvent;
import io.workshop.funtastic.model.TicketDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1")
public class TicketsController {
    private final DelayProperties delayProperties;
    private final FailureProperties failureProperties;
    private final Faker faker;

    @Autowired
    public TicketsController(DelayProperties delayProperties, FailureProperties failureProperties) {
        this.delayProperties = delayProperties;
        this.failureProperties = failureProperties;
        this.faker = new Faker();
    }

    @GetMapping("/concert")
    public Flux<TicketDto> loadConcerts() {
        simulateFailure(failureProperties.getConcert());
        return Flux.fromStream(IntStream.range(0, 10)
                .boxed()
                .map(x -> TicketDto.builder()
                        .name(faker.rockBand().name())
                        .event(FunEvent.CONCERT)
                        .date(faker.date().future(365, TimeUnit.DAYS))
                        .city(faker.address().city())
                        .build())
        ).delaySubscription(delayProperties.getConcert());
    }

    @GetMapping("/exhibition")
    public Flux<TicketDto> loadExhibitions() {
        simulateFailure(failureProperties.getExhibition());
        return Flux.fromStream(IntStream.range(0, 10)
                .boxed()
                .map(x -> TicketDto.builder()
                        .name(faker.artist().name())
                        .event(FunEvent.EXHIBITION)
                        .date(faker.date().future(365, TimeUnit.DAYS))
                        .city(faker.address().city())
                        .build())
        ).delaySubscription(delayProperties.getExhibition());
    }

    @GetMapping("/sport")
    public Flux<TicketDto> loadGames() {
        simulateFailure(failureProperties.getSport());
        return Flux.fromStream(IntStream.range(0, 10)
                .boxed()
                .map(x -> TicketDto.builder()
                        .name(faker.team().name() + " vs " + faker.team().name())
                        .event(FunEvent.SPORT)
                        .date(faker.date().future(365, TimeUnit.DAYS))
                        .city(faker.address().city())
                        .build())
        ).delaySubscription(delayProperties.getSport());
    }

    private void simulateFailure(Double chance) {
        if (chance > 0 && Math.random() < chance) {
            throw new RuntimeException();
        }
    }

}
