package com.example.resilient_api.infrastructure.adapters.webclient;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
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
public class UserWebClient implements UserExternalServicePort {

    private static final String X_MESSAGE_ID = "X-Message-Id";
    private final WebClient.Builder webClientBuilder;

    @Value("${external.user.base-url}")
    private String userBaseUrl;

    @Override
    public Flux<UserEnrollment> getUsersByIds(List<Long> userIds, String messageId) {
        log.info("Calling user service to get users by IDs with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(userBaseUrl + "/users/by-ids")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new UserIdsRequest(userIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            log.error("User service returned 5xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.USER_SERVICE_ERROR));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            log.error("User service returned 4xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(TechnicalMessage.USER_SERVICE_ERROR));
                        })
                .bodyToFlux(new ParameterizedTypeReference<UserResponse>() {})
                .map(response -> UserEnrollment.builder()
                        .userId(response.getId())
                        .userName(response.getName())
                        .userEmail(response.getEmail())
                        .build())
                .doOnComplete(() -> log.info("Successfully retrieved users by IDs with messageId: {}", messageId))
                .doOnError(error -> log.error("Error retrieving users by IDs with messageId: {}", messageId, error));
    }
}
