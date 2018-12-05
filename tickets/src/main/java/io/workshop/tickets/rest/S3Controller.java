package io.workshop.tickets.rest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import io.workshop.tickets.GifSequenceWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.ryanair.aws.xray.reactive.XRayAwareMonoBuilder.wrapWithXRay;

@Slf4j
@RestController
@RequestMapping("/s3")
public class S3Controller {
    private static final String INPUT_BUCKET = "landsat-pds";
    private static final String OUTPUT_BUCKET = "xrayworkshopbucketremoveme";
    private static final String imgSuffix = "large.jpg";
    private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
            .withForceGlobalBucketAccessEnabled(true)
            .build();

    @GetMapping
    public Mono<List<S3ObjectSummary>> loadFilesList() {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(INPUT_BUCKET)
                .withMaxKeys(500) // TODO: Play with this value and watch result on xray
                .withPrefix("c1/L8/139/045/");

        Flux<List<S3ObjectSummary>> paginator = Flux.generate(
                () -> Tuples.of(request, s3.listObjectsV2(request)),
                (requestResult, sink) -> {
                    try {
                        String nextContinuationToken = requestResult.getT2().getNextContinuationToken();
                        ListObjectsV2Request nextPageRequest = requestResult.getT1().withContinuationToken(nextContinuationToken);
                        ListObjectsV2Result listingResult = s3.listObjectsV2(nextPageRequest);

                        if (!listingResult.getObjectSummaries().isEmpty()) {
                            sink.next(listingResult.getObjectSummaries());
                        }
                        if (listingResult.getObjectSummaries().isEmpty() || nextContinuationToken == null) {
                            sink.complete();
                        }
                        return Tuples.of(nextPageRequest, listingResult);
                    } catch (Exception e) {
                        sink.error(e);
                    }
                    return null;
                });
        return paginator
                .flatMapIterable(x -> x)
                .filter(x -> x.getKey().endsWith(imgSuffix))
                .collectSortedList(Comparator.comparing(S3ObjectSummary::getKey));
    }

    @GetMapping(path = "/giphy", produces = MediaType.IMAGE_GIF_VALUE)
    public Mono<byte[]> loadFileAnimation() throws Exception {
        return loadFilesList()
                .flux()
                .flatMapIterable(x -> x)
                .flatMap(this::loadS3Object)
                .flatMap(this::readImage)
                .filter(Objects::nonNull)
                .collectSortedList(Comparator.comparing(Tuple2::getT1))
                .map(this::buildGif)
                .doOnSuccess(this::persistToS3);
    }

    private Mono<S3Object> loadS3Object(S3ObjectSummary x) {
        return wrapWithXRay(Mono.fromSupplier(() -> s3.getObject(x.getBucketName(), x.getKey())))
                .subscribeOn(Schedulers.elastic());
    }

    private Mono<Tuple2<String, BufferedImage>> readImage(S3Object x) {
        return Mono.defer(() -> {
            try (S3ObjectInputStream objectContent = x.getObjectContent()) {
                return Mono.just(Tuples.of(x.getKey(), ImageIO.read(objectContent)));
            } catch (IOException e) {
                log.warn("Couldn't load image {}", x.getKey());
                return Mono.empty();
            }
        });
    }

    private byte[] buildGif(List<Tuple2<String, BufferedImage>> images) {
        int imageType = images.stream()
                .map(Tuple2::getT2)
                .findFirst()
                .map(BufferedImage::getType)
                .orElse(1);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        AWSXRay.createSubsegment("Build gif out of images", () -> {
            try (ImageOutputStream imageStream = new MemoryCacheImageOutputStream(bytes)) {
                images.stream()
                        .map(Tuple2::getT2)
                        .reduce(new GifSequenceWriter(imageStream, imageType, 100, true), (writer, image) -> {
                            AWSXRay.createSubsegment("Add image to gif", () -> {
                                try {
                                    writer.writeToSequence(image);
                                } catch (IOException e) {
                                    log.error("Couldn't add image to gif", e);
                                }
                            });
                            return writer;
                        }, (x, y) -> x)
                        .close();
            } catch (IOException e) {
                log.error("Couldn't build gif", e);
            }
        });
        return bytes.toByteArray();
    }

    private void persistToS3(byte[] content) {
        String fileName = "satellite-" + UUID.randomUUID().toString() + ".gif";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(MediaType.IMAGE_GIF_VALUE);
        metadata.setHeader("filename", fileName);
        metadata.setContentLength(content.length);
        s3.putObject(new PutObjectRequest(
                OUTPUT_BUCKET,
                fileName,
                new ByteArrayInputStream(content),
                metadata));
    }
}
