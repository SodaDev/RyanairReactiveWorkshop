package io.workshop.ticketsmvc.rest;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import io.workshop.ticketsmvc.model.RyanairTicketDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@RestController
@RequestMapping(("/tickets"))
public class RyanairTicketsMvcController {

    @Autowired
    private RestTemplate restTemplate;
    private ParameterizedTypeReference<List<RyanairTicketDto>> type = new ParameterizedTypeReference<List<RyanairTicketDto>>() {};

    @GetMapping
    public DeferredResult<List<RyanairTicketDto>> loadTickets() {
        DeferredResult<List<RyanairTicketDto>> deferredResult = new DeferredResult<>();
        Observable.just("concert", "exhibition", "sport")
                .flatMap(x -> new TicketsCommand(x).toObservable())
                .flatMapIterable(x -> x)
                .toList()
                .subscribe(deferredResult::setResult, deferredResult::setErrorResult);

        return deferredResult;
    }

    @GetMapping("/sync")
    public List<RyanairTicketDto> loadTicketsSync() {
        return Stream.of("concert", "exhibition", "sport")
                .map(path -> "http://localhost:8080/api/v1/" + path)
                .map(this::loadTicketsFromThirdParty)
                .flatMap(Stream::of)
                .collect(Collectors.toList());
    }

    @GetMapping("/parallel")
    public List<RyanairTicketDto> loadTicketsParallel() {
        Entity traceEntity = AWSXRay.getTraceEntity();
        return Stream.of("concert", "exhibition", "sport")
                .map(path -> "http://localhost:8080/api/v1/" + path)
                .parallel()
                .map(x -> {
                    AWSXRay.setTraceEntity(traceEntity);
                    return loadTicketsFromThirdParty(x);
                })
                .flatMap(Stream::of)
                .collect(Collectors.toList());
    }

    private RyanairTicketDto[] loadTicketsFromThirdParty(String url) {
        try {
            return restTemplate.getForObject(url, RyanairTicketDto[].class);
        } catch (RestClientException e) {
            log.error("Request error occurred", e);
            return new RyanairTicketDto[]{};
        }
    }

    public class TicketsCommand extends HystrixCommand<List<RyanairTicketDto>> {
        private final String path;

        public TicketsCommand(final String path) {
            super(Setter.withGroupKey(
                    HystrixCommandGroupKey.Factory.asKey("ticketsPool"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("tickets" + path)));
            this.path = path;
        }

        @Override
        protected List<RyanairTicketDto> run() {
            return restTemplate.exchange("http://localhost:8080/api/v1/" + path, GET, null, type).getBody();
        }

        @Override
        protected List<RyanairTicketDto> getFallback() {
            return Collections.emptyList();
        }
    }
}
