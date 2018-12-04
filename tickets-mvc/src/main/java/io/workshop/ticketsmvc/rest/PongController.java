package io.workshop.ticketsmvc.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pong")
public class PongController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public Map pong(@RequestParam("times") int times) {
        if (times <= 0) {
            return restTemplate.getForObject("https://aggregator-dev2-rooms.ryanair.com/ha/insights/health", Map.class);
        }
        String pingUrl = "http://ticketswebflux:9091/ping?times=" + times;
        return restTemplate.getForObject(pingUrl, Map.class);
    }
}
