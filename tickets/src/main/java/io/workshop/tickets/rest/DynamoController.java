package io.workshop.tickets.rest;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/dynamo")
public class DynamoController {
    private static final String DYNAMO_TABLE_NAME = "XRayWorkshopTableRemoveMe";
    private AmazonDynamoDBAsync dynamoDb = AmazonDynamoDBAsyncClientBuilder.standard()
            .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
            .build();

    @GetMapping("/putItems")
    public Flux<PutItemResult> loadDataFromDynamoDb() {
        return Flux.range(0, 5)
                .flatMap(x -> Mono.create(sink ->
                        dynamoDb.putItemAsync(DYNAMO_TABLE_NAME, buildSomeDocument(), new ReactorAsyncHandler(sink))
                ));
    }

    @GetMapping("/putBrokenItems")
    public Mono<PutItemResult> putBrokenDocToDynamoDb() {
        return Mono.create(sink ->
                dynamoDb.putItemAsync(DYNAMO_TABLE_NAME, buildBrokenDocument(), new ReactorAsyncHandler(sink))
        );
    }

    @GetMapping("/scan")
    public Mono<PutItemResult> scanItemsFromDynamo() {
        return Mono.create(sink ->
                dynamoDb.scanAsync(new ScanRequest(DYNAMO_TABLE_NAME), new ReactorAsyncHandler(sink))
        );
    }

    @RequiredArgsConstructor
    private class ReactorAsyncHandler<REQ extends AmazonWebServiceRequest, RES> implements AsyncHandler<REQ, RES> {
        private final MonoSink<? super RES> sink;

        @Override
        public void onError(Exception e) {
            log.error("Couldn't persist on Dynamo.", e);
            sink.error(e);
        }

        @Override
        public void onSuccess(REQ request, RES response) {
            sink.success(response);
        }

    }

    private Map<String, AttributeValue> buildSomeDocument() {
        return new HashMap<String, AttributeValue>() {{
            put("id", new AttributeValue(UUID.randomUUID().toString()));
            put("time", new AttributeValue(LocalDateTime.now().toString()));
        }};
    }

    private Map<String, AttributeValue> buildBrokenDocument() {
        return new HashMap<String, AttributeValue>() {{
            put("time", new AttributeValue(LocalDateTime.now().toString()));
        }};
    }
}
