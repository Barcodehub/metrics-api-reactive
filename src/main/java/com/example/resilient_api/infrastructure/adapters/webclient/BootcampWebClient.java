package com.example.resilient_api.infrastructure.adapters.webclient;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.CapacityDetail;
import com.example.resilient_api.domain.model.TechnologyDetail;
import com.example.resilient_api.domain.spi.BootcampExternalServicePort;
import com.example.resilient_api.domain.spi.BootcampInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampWebClient implements BootcampExternalServicePort {

    private static final String X_MESSAGE_ID = "X-Message-Id";
    private final WebClient.Builder webClientBuilder;

    @Value("${external.bootcamp.base-url}")
    private String bootcampBaseUrl;

    @Override
    public Mono<BootcampInfo> getBootcampById(Long bootcampId, String messageId) {
        log.info("Calling bootcamp service to get bootcamp by id: {} with messageId: {}", bootcampId, messageId);

        return webClientBuilder.build()
                .get()
                .uri(bootcampBaseUrl + "/bootcamp/" + bootcampId)
                .header(X_MESSAGE_ID, messageId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            log.error("Bootcamp service returned 5xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.BOOTCAMP_SERVICE_ERROR));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            log.error("Bootcamp service returned 4xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.BOOTCAMP_NOT_FOUND));
                        })
                .bodyToMono(BootcampInfoResponse.class)
                .map(response -> {
                    // Mapear capacidades completas con tecnologías
                    List<CapacityDetail> capacities = response.getCapacities() != null
                        ? response.getCapacities().stream()
                            .map(cap -> CapacityDetail.builder()
                                    .capacityId(cap.getId())
                                    .capacityName(cap.getName())
                                    .technologies(cap.getTechnologies() != null
                                            ? cap.getTechnologies().stream()
                                                .map(tech -> TechnologyDetail.builder()
                                                        .technologyId(tech.getId())
                                                        .technologyName(tech.getName())
                                                        .build())
                                                .collect(Collectors.toList())
                                            : List.of())
                                    .build())
                            .collect(Collectors.toList())
                        : List.of();

                    return BootcampInfo.builder()
                            .id(response.getId())
                            .name(response.getName())
                            .description(response.getDescription())
                            .launchDate(response.getLaunchDate())
                            .duration(response.getDuration())
                            .capacities(capacities)
                            .build();
                })
                .doOnSuccess(result -> log.info("Successfully retrieved bootcamp {} with messageId: {}", bootcampId, messageId))
                .doOnError(error -> log.error("Error retrieving bootcamp {} with messageId: {}", bootcampId, messageId, error));
    }

    @Override
    public Flux<Long> getUserIdsByBootcampId(Long bootcampId, String messageId) {
        log.info("Calling bootcamp service to get user IDs for bootcamp: {} with messageId: {}", bootcampId, messageId);

        return webClientBuilder.build()
                .get()
                .uri(bootcampBaseUrl + "/bootcamp/" + bootcampId + "/users")
                .header(X_MESSAGE_ID, messageId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            log.error("Bootcamp service returned 5xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.BOOTCAMP_SERVICE_ERROR));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            log.warn("No users found for bootcamp {} with messageId: {}", bootcampId, messageId);
                            return Mono.empty();
                        })
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
                .flatMapMany(Flux::fromIterable)
                .doOnComplete(() -> log.info("Successfully retrieved user IDs for bootcamp {} with messageId: {}", bootcampId, messageId))
                .doOnError(error -> log.error("Error retrieving user IDs for bootcamp {} with messageId: {}", bootcampId, messageId, error));
    }
}
