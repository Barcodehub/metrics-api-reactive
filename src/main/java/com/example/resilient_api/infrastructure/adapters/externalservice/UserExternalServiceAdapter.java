package com.example.resilient_api.infrastructure.adapters.externalservice;

import com.example.resilient_api.domain.model.UserEnrollment;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import com.example.resilient_api.infrastructure.adapters.webclient.UserWebClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
public class UserExternalServiceAdapter implements UserExternalServicePort {

    private final UserWebClient userWebClient;

    @Override
    public Flux<UserEnrollment> getUsersByIds(List<Long> userIds, String messageId) {
        return userWebClient.getUsersByIds(userIds, messageId);
    }
}
