package io.workshop.tickets.rest;

import com.amazonaws.xray.AWSXRay;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@Slf4j
@RestControllerAdvice
public class TicketsWebFluxExceptionHandler {

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseBody
    ErrorResult handleValidationException(final WebExchangeBindException ex) {
        log.warn("Got bad request with: ", ex);
        markXrayFailure(ex);
        return ErrorResult.builder()
                .type("VALIDATION_ERROR")
                .message("Validation failed")
                .build();
    }

    // Generic
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ErrorResult handleThrowable(final Throwable ex) {
        log.warn("Unhandled exception occurred inside container", ex);
        markXrayFailure(ex);
        return ErrorResult.builder()
                .type("SERVER_ERROR")
                .message("Something went wrong")
                .build();
    }

    private void markXrayFailure(final Throwable ex) {
        AWSXRay.getCurrentSegmentOptional()
                .ifPresent(x -> {
                    x.addException(ex);
                    x.setFault(true);
                    x.setError(true);
                });
    }

    @Data
    @Builder
    private static class ErrorResult {
        private String type;
        private String message;
    }
}