package ru.mifi.practice.booking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.mifi.practice.booking.client.dto.AvailabilityRequest;
import ru.mifi.practice.booking.client.dto.AvailabilityResponse;
import ru.mifi.practice.booking.client.dto.RoomRecommendation;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotelClient {

    private final WebClient.Builder webClientBuilder;

    public AvailabilityResponse confirmAvailability(Long roomId,
                                                    LocalDate start,
                                                    LocalDate end,
                                                    String requestId,
                                                    String token) {
        AvailabilityRequest body = new AvailabilityRequest(start, end, requestId);
        return webClientBuilder.build()
                .post()
                .uri("http://hotel-service/api/rooms/{id}/confirm-availability", roomId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.createException().flatMap(Mono::error))
                .bodyToMono(AvailabilityResponse.class)
                .timeout(Duration.ofSeconds(3))
                .retryWhen(defaultRetry())
                .block();
    }

    public void release(Long roomId, String requestId, String token) {
        AvailabilityRequest body = new AvailabilityRequest(null, null, requestId);
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://hotel-service/api/rooms/{id}/release", roomId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(token))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
        } catch (Exception ex) {
            log.warn("Release request {} failed: {}", requestId, ex.getMessage());
        }
    }

    public List<RoomRecommendation> recommend(String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://hotel-service/api/rooms/recommend")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .retrieve()
                .bodyToFlux(RoomRecommendation.class)
                .collectList()
                .timeout(Duration.ofSeconds(3))
                .retryWhen(defaultRetry())
                .block();
    }

    private Retry defaultRetry() {
        return Retry.backoff(3, Duration.ofMillis(200))
                .maxBackoff(Duration.ofSeconds(2))
                .filter(this::shouldRetry)
                .doBeforeRetry(retrySignal -> {
                    log.warn("Retrying request, attempt {}/3, delay: {}ms", 
                            retrySignal.totalRetries() + 1, 
                            retrySignal.totalRetriesInARow() * 200);
                })
                .onRetryExhaustedThrow((spec, signal) -> {
                    log.error("Retry exhausted after {} attempts", signal.totalRetries());
                    return signal.failure();
                });
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            return true;
        }
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private String bearer(String token) {
        return token != null ? "Bearer " + token : "";
    }
}
