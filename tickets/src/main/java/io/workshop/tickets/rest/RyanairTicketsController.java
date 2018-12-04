package io.workshop.tickets.rest;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.ryanair.aws.xray.reactive.XRayAwareMonoBuilder;
import com.ryanair.aws.xray.reactive.XRayWebClientFilter;
import io.workshop.tickets.model.RyanairTicketDto;
import org.springframework.cloud.netflix.hystrix.HystrixCommands;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController("/tickets")
public class RyanairTicketsController {

    private final WebClient funtasticClient = WebClient.builder()
            .baseUrl("http://localhost:8080/api/v1/")
            .filter(new XRayWebClientFilter())
            .build();

    @GetMapping
    public Flux<RyanairTicketDto> loadTickets() {
        Entity traceEntity = AWSXRay.getTraceEntity();
        return Flux.just("concert", "exhibition", "sport")
                .flatMap(path -> {
                            AWSXRay.setTraceEntity(traceEntity);
                            return HystrixCommands.from(XRayAwareMonoBuilder.wrapWithXRay(funtasticClient
                                    .get()
                                    .uri(path)
                                    .retrieve()
                                    .bodyToFlux(RyanairTicketDto.class)
                            ))
                                    .commandName("tickets" + path)
                                    .groupName("ticketsPool")
                                    .fallback(Flux.empty())
                                    .toFlux();
                        }
                );
    }
}
