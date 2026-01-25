package com.example.resilient_api.infrastructure.adapters.webclient;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.TechnologyDetail;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityWebClient implements CapacityExternalServicePort {

    private static final String X_MESSAGE_ID = "X-Message-Id";
    private final WebClient.Builder webClientBuilder;

    @Value("${external.capacity.base-url}")
    private String capacityBaseUrl;

    @Override
    public Flux<CapacityDetail> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId) {
        log.info("Calling capacity service to get capacities with technologies with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(capacityBaseUrl + "/with-technologies")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new CapacityIdsRequest(capacityIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            log.error("Capacity service returned 5xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.CAPACITY_SERVICE_ERROR));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            log.error("Capacity service returned 4xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.CAPACITY_SERVICE_ERROR));
                        })
                .bodyToFlux(new ParameterizedTypeReference<CapacitySummaryResponse>() {})
                .map(response -> CapacityDetail.builder()
                        .capacityId(response.getId())
                        .capacityName(response.getName())
                        .technologies(response.getTechnologies().stream()
                                .map(tech -> TechnologyDetail.builder()
                                        .technologyId(tech.getId())
                                        .technologyName(tech.getName())
                                        .build())
                                .toList())
                        .build())
                .doOnComplete(() -> log.info("Successfully retrieved capacities with technologies with messageId: {}", messageId))
                .doOnError(error -> log.error("Error retrieving capacities with technologies with messageId: {}", messageId, error));
    }
}
