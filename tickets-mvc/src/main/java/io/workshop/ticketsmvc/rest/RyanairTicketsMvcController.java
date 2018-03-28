package io.workshop.ticketsmvc.rest;

import io.workshop.ticketsmvc.model.RyanairTicketDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@RestController("/tickets")
public class RyanairTicketsMvcController {

    @Autowired
    private RestTemplate restTemplate;
    private ParameterizedTypeReference<List<RyanairTicketDto>> type = new ParameterizedTypeReference<List<RyanairTicketDto>>() {};

    @GetMapping
    public List<RyanairTicketDto> loadTickets() {
        return Stream.of("concert", "exhibition", "sport")
                .map(path -> "http://localhost:8080/api/v1/" + path)
                .parallel()
                .map(url -> restTemplate.exchange(url, GET, null, type))
                .map(HttpEntity::getBody)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
